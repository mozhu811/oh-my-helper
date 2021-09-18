package io.cruii.bilibili.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tencentcloudapi.scf.v20180416.models.*;
import io.cruii.bilibili.config.TencentApiConfig;
import io.cruii.bilibili.dto.ContainerDTO;
import io.cruii.bilibili.dto.CreateContainerDTO;
import io.cruii.bilibili.entity.ContainerConfig;
import io.cruii.bilibili.service.ContainerService;
import io.cruii.bilibili.util.ScfUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Service
@Log4j2
public class ContainerServiceImpl implements ContainerService {

    private final TencentApiConfig apiConfig;

    private final Executor bilibiliExecutor;

    @Value("${scf.jar-location}")
    private String jarLocation;

    public ContainerServiceImpl(TencentApiConfig apiConfig,
                                Executor bilibiliExecutor) {
        this.apiConfig = apiConfig;
        this.bilibiliExecutor = bilibiliExecutor;
    }

    /**
     * 本地缓存
     */
    private static final List<ContainerDTO> CONTAINER_CACHE = new ArrayList<>();

    /**
     * 锁对象
     */
    private final Object lock = new Object();

    @Override
    public List<ContainerDTO> listContainers() {
        synchronized (lock) {
            if (!CONTAINER_CACHE.isEmpty()) {
                return CONTAINER_CACHE.stream()
                        .sorted((o1, o2) -> {
                            if (o1.getCurrentExp() != null && o2.getCurrentExp() != null && Objects.equals(o1.getLevel(), o2.getLevel())) {
                                return o2.getCurrentExp() - o1.getCurrentExp();
                            } else {
                                if (o1.getCurrentExp() == null) {
                                    return 1;
                                } else if (o2.getCurrentExp() == null) {
                                    return -1;
                                } else {
                                    return o2.getLevel() - o1.getLevel();
                                }
                            }
                        }).collect(Collectors.toList());
            }

        ListFunctionsResponse listFunctionsResponse = ScfUtil.listFunctions(apiConfig);

        for (int i = 0; i < listFunctionsResponse.getFunctions().length / 20 + 1; i++) {
            List<ContainerDTO> list = Arrays.stream(listFunctionsResponse.getFunctions())
                    .skip(i * 20L)
                    .limit(20)
                    .parallel()
                    .map(function -> {
                        GetFunctionResponse getFunctionResponse = ScfUtil.getFunction(apiConfig, function.getFunctionName());
                        Environment environment = getFunctionResponse.getEnvironment();
                        Variable[] variables = environment.getVariables();
                        return Arrays.stream(variables)
                                .map(Variable::getValue)
                                .filter(JSONUtil::isJsonObj)
                                .map(obj -> JSONUtil.parseObj(obj).set("containerName", function.getFunctionName()).toBean(ContainerConfig.class))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("无法获取该容器配置, name: " + function.getFunctionName()));
                    })
                    .map(this::getContainerInfo)
                    .collect(Collectors.toList());
            // 将容器信息缓存
            CONTAINER_CACHE.addAll(list);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        return CONTAINER_CACHE.stream()
                .sorted((o1, o2) -> {
                    if (o1.getCurrentExp() != null && o2.getCurrentExp() != null && Objects.equals(o1.getLevel(), o2.getLevel())) {
                        return o2.getCurrentExp() - o1.getCurrentExp();
                    } else {
                        if (o1.getCurrentExp() == null) {
                            return 1;
                        } else if (o2.getCurrentExp() == null) {
                            return -1;
                        } else {
                            return o2.getLevel() - o1.getLevel();
                        }
                    }
                }).collect(Collectors.toList());
        }
    }

    @Override
    public ContainerDTO createContainer(CreateContainerDTO createContainerDTO) {
        log.info("传入参数: {}", createContainerDTO);
        ScfUtil.createFunction(apiConfig, createContainerDTO, jarLocation);
        String containerName = createContainerDTO.getContainerName();
        boolean created = false;
        while (!created) {
            GetFunctionResponse getFunctionResponse = ScfUtil.getFunction(apiConfig, containerName);
            String status = getFunctionResponse.getStatus();
            created = "Active".equals(status);
            if ("CreateFailed".equals(status)) {
                removeContainer(containerName);
                throw new RuntimeException("创建容器失败");
            }
        }

        ScfUtil.createTrigger(apiConfig, containerName, "0 10 0 * * * *");

        bilibiliExecutor.execute(() -> ScfUtil.executeFunction(apiConfig, containerName));

        // 清除缓存
        CONTAINER_CACHE.clear();

        // 获取用户B站数据
        return getContainerInfo(createContainerDTO.getConfig());
    }

    private ContainerDTO getContainerInfo(ContainerConfig config) {
        String sessdata = config.getSessdata();
        Integer dedeuserid = config.getDedeuserid();
        HttpCookie sessdataCookie = new HttpCookie("SESSDATA", sessdata);
        HttpCookie dedeUserID = new HttpCookie("DedeUserID", String.valueOf(dedeuserid));
        sessdataCookie.setDomain(".bilibili.com");
        dedeUserID.setDomain(".bilibili.com");
        String body = HttpRequest.get("https://api.bilibili.com/x/web-interface/nav")
                .cookie(sessdataCookie)
                .execute().body();
        log.info("请求B站用户信息结果: {}", body);
        Integer code = JSONUtil.parseObj(body).getInt("code");
        if (code == -101) {
            log.error("账号Cookie已失效, {}, {}", sessdata, dedeuserid);
        }
        JSONObject data = JSONUtil.parseObj(body).getJSONObject("data");
        Boolean isLogin = data.getBool("isLogin");
        if (Boolean.FALSE.equals(isLogin)) {
            JSONObject baseInfo = JSONUtil.parseObj(HttpRequest.get("https://api.bilibili.com/x/space/acc/info?mid=" + dedeuserid).execute().body()).getJSONObject("data");
            InputStream avatarStream = HttpRequest.get(baseInfo.getStr("face"))
                    .execute().bodyStream();
            StringBuilder sb = new StringBuilder();
            String username = baseInfo.getStr("name");
            if (username.length() > 2) {
                for (int i = 0; i < username.length(); i++) {
                    if (i > 0 && i < username.length() - 1) {
                        sb.append("*");
                    } else {
                        sb.append(username.charAt(i));
                    }
                }
            } else {
                sb.append(username.charAt(0)).append("*");
            }
            return ContainerDTO.builder()
                    .containerName(config.getContainerName())
                    .dedeuserid(dedeuserid)
                    .username(sb.toString())
                    .avatar("data:image/jpeg;base64," + Base64.encode(avatarStream))
                    .level(baseInfo.getInt("level"))
                    .isLogin(false).build();
        }
        InputStream avatarStream = HttpRequest.get(data.getStr("face"))
                .execute().bodyStream();
        StringBuilder sb = new StringBuilder();
        String username = data.getStr("uname");
        if (username.length() > 2) {
            for (int i = 0; i < username.length(); i++) {
                if (i > 0 && i < username.length() - 1) {
                    sb.append("*");
                } else {
                    sb.append(username.charAt(i));
                }
            }
        } else {
            sb.append(username.charAt(0)).append("*");
        }
        String coins = data.getStr("money");

        JSONObject vip = data.getJSONObject("vip");
        JSONObject levelInfo = data.getJSONObject("level_info");
        Integer currentLevel = levelInfo.getInt("current_level");
        return ContainerDTO.builder()
                .containerName(config.getContainerName())
                .isLogin(isLogin)
                .dedeuserid(dedeuserid)
                .username(sb.toString())
                .avatar("data:image/jpeg;base64," + Base64.encode(avatarStream))
                .coins(coins == null ? "——" : coins)
                .level(currentLevel)
                .currentExp(levelInfo.getInt("current_exp"))
                .nextExp(currentLevel == 6 ? 0 : levelInfo.getInt("next_exp"))
                .vipType(vip.getInt("type"))
                .dueDate(vip.getLong("due_date"))
                .key(SecureUtil.md5(String.valueOf(dedeuserid))).build();
    }

    @Override
    public void updateTrigger(String containerName, String cronExpression) {
        ScfUtil.createTrigger(apiConfig, containerName, cronExpression);
    }

    @Override
    public void removeContainer(String containerName) {
        ScfUtil.deleteFunction(apiConfig, containerName);
        CONTAINER_CACHE.clear();
    }

    @Override
    public void removeContainer(Integer dedeuserid) {
        AtomicBoolean success = new AtomicBoolean(false);
        CONTAINER_CACHE.stream()
                .filter(c -> c.getDedeuserid().equals(dedeuserid))
                .forEach(container -> {
                    String containerName = container.getContainerName();
                    ScfUtil.deleteFunction(apiConfig, containerName);
                    success.set(true);
                    log.info("删除容器[{} - {}]成功", dedeuserid, containerName);
                });
        if (!success.get()) {
            ListFunctionsResponse listFunctionsResponse = ScfUtil.listFunctions(apiConfig);
            String containerName = Arrays.stream(listFunctionsResponse.getFunctions()).filter(function -> {
                String description = function.getDescription();
                String[] cookies = description.split(";");
                return Integer.valueOf(cookies[0]).equals(dedeuserid);
            }).map(Function::getFunctionName).findFirst().orElseThrow(() -> new RuntimeException("容器不存在"));
            ScfUtil.deleteFunction(apiConfig, containerName);
            log.info("删除容器[{} - {}]成功", dedeuserid, containerName);
        }
        CONTAINER_CACHE.clear();
    }

    @Override
    public void updateCookies(Integer dedeuserid, String sessdata, String biliJct) {
        ScfUtil.updateFunction(apiConfig, dedeuserid, sessdata, biliJct);
        CONTAINER_CACHE.clear();
    }
}
