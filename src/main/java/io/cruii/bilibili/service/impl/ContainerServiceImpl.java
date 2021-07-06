package io.cruii.bilibili.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tencentcloudapi.scf.v20180416.models.Environment;
import com.tencentcloudapi.scf.v20180416.models.GetFunctionResponse;
import com.tencentcloudapi.scf.v20180416.models.ListFunctionsResponse;
import com.tencentcloudapi.scf.v20180416.models.Variable;
import io.cruii.bilibili.config.TencentApiConfig;
import io.cruii.bilibili.dto.ContainerDTO;
import io.cruii.bilibili.dto.CreateContainerDTO;
import io.cruii.bilibili.service.ContainerService;
import io.cruii.bilibili.util.ScfUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpCookie;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
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

    @Override
    public List<ContainerDTO> listContainers() {
        ListFunctionsResponse listFunctionsResponse = ScfUtil.listFunctions(apiConfig);

        return Arrays.stream(listFunctionsResponse.getFunctions()).map(f -> {
            GetFunctionResponse getFunctionResponse = ScfUtil.getFunction(apiConfig, f.getFunctionName());
            Environment environment = getFunctionResponse.getEnvironment();
            Variable[] variables = environment.getVariables();
            return Arrays.stream(variables)
                    .map(Variable::getValue)
                    .filter(JSONUtil::isJsonObj)
                    .map(JSONUtil::parseObj)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("无法获取该容器配置, name: " + f.getFunctionName()));
        }).map(config ->
                getContainerInfo(config.getStr("sessdata"), config.getInt("dedeuserid")))
                .collect(Collectors.toList());
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

        // 获取用户B站数据
        return getContainerInfo(createContainerDTO.getConfig().getSessdata(),
                createContainerDTO.getConfig().getDedeuserid());
    }

    private ContainerDTO getContainerInfo(String sessdata, Integer dedeuserid) {
        log.debug("获取容器信息入参: {}, {}", sessdata, dedeuserid);
        HttpCookie sessdataCookie = new HttpCookie("SESSDATA", sessdata);
        HttpCookie dedeUserID = new HttpCookie("DedeUserID", String.valueOf(dedeuserid));
        sessdataCookie.setDomain(".bilibili.com");
        dedeUserID.setDomain(".bilibili.com");
        String body = HttpRequest.get("https://api.bilibili.com/x/web-interface/nav")
                .cookie(sessdataCookie)
                .execute().body();
        log.info("请求B站用户信息结果: {}", body);
        JSONObject data = JSONUtil.parseObj(body).getJSONObject("data");
        Boolean isLogin = data.getBool("isLogin");
        if (Boolean.FALSE.equals(isLogin)) {
            return ContainerDTO.builder()
                    .dedeuserid(dedeuserid)
                    .isLogin(false).build();
        }
        InputStream avatarStream = HttpRequest.get(data.getStr("face"))
                .execute().bodyStream();
        StringBuilder sb = new StringBuilder();
        String username = data.getStr("uname");
        for (int i = 0; i < username.length(); i++) {
            if (i > 0 && i < username.length() - 1) {
                sb.append("*");
            } else {
                sb.append(username.charAt(i));
            }
        }
        String coinResp = HttpRequest.get("https://account.bilibili.com/site/getCoin")
                .cookie(sessdataCookie, dedeUserID)
                .execute().body();
        String coins = null;
        if (JSONUtil.isJsonObj(coinResp)) {
            JSONObject coinData = JSONUtil.parseObj(coinResp).getJSONObject("data");

            coins = coinData.getStr("money");
        }
        JSONObject vip = data.getJSONObject("vip");
        JSONObject levelInfo = data.getJSONObject("level_info");
        Integer currentLevel = levelInfo.getInt("current_level");
        return ContainerDTO.builder()
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
                .key(SecureUtil.md5(sessdata)).build();
    }

    @Override
    public void updateTrigger(String containerName, String cronExpression) {
        ScfUtil.createTrigger(apiConfig, containerName, cronExpression);
    }

    @Override
    public void removeContainer(String containerName) {
        ScfUtil.deleteFunction(apiConfig, containerName);
    }

    @Override
    public ContainerDTO updateCookies(Integer dedeuserid, String sessdata, String biliJct) {
        ScfUtil.updateFunction(apiConfig, dedeuserid, sessdata, biliJct);

        GetFunctionResponse function = ScfUtil.getFunction(apiConfig, dedeuserid);
        bilibiliExecutor.execute(() -> ScfUtil.executeFunction(apiConfig, function.getFunctionName()));
        // 获取用户B站数据
        return getContainerInfo(sessdata, dedeuserid);
    }
}
