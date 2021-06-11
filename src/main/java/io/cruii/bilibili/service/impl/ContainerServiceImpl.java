package io.cruii.bilibili.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.scf.v20180416.models.*;
import io.cruii.bilibili.config.TencentApiConfig;
import io.cruii.bilibili.dto.ContainerDTO;
import io.cruii.bilibili.dto.CreateContainerDTO;
import io.cruii.bilibili.service.ContainerService;
import io.cruii.bilibili.util.ScfClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        com.tencentcloudapi.scf.v20180416.ScfClient scfClient = new ScfClient.Builder(apiConfig).build();
        ListFunctionsRequest req = new ListFunctionsRequest();
        ListFunctionsResponse resp;
        try {
            resp = scfClient.ListFunctions(req);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("获取容器列表失败", e);
        }

        GetFunctionRequest getFunctionRequest = new GetFunctionRequest();

        return Arrays.stream(resp.getFunctions()).map(f -> {
            getFunctionRequest.setFunctionName(f.getFunctionName());
            try {
                GetFunctionResponse getFunctionResponse = scfClient.GetFunction(getFunctionRequest);
                Environment environment = getFunctionResponse.getEnvironment();
                Variable[] variables = environment.getVariables();
                return Arrays.stream(variables)
                        .map(Variable::getValue)
                        .filter(JSONUtil::isJsonObj)
                        .map(JSONUtil::parseObj).findFirst().orElseThrow(IllegalArgumentException::new);
            } catch (TencentCloudSDKException | IllegalArgumentException e) {
                throw new RuntimeException("获取容器详细信息失败: " + f.getFunctionName(), e);
            }
        }).map(config ->
                getContainerInfo(config.getStr("sessdata"), config.getStr("dedeuserid")))
                .collect(Collectors.toList());
    }

    @Override
    public ContainerDTO createContainer(CreateContainerDTO createContainerDTO) throws FileNotFoundException {
        log.info("传入参数: {}", createContainerDTO);
        String dedeuserid = createContainerDTO.getDedeuserid();
        String sessdata = createContainerDTO.getSessdata();
        String biliJct = createContainerDTO.getBiliJct();

        boolean created = false;
        // 构建ScfClient
        com.tencentcloudapi.scf.v20180416.ScfClient scfClient = new ScfClient.Builder(apiConfig).build();
        CreateFunctionRequest req = new CreateFunctionRequest();

        // 设置请求参数
        String containerName = createContainerDTO.getContainerName();
        req.setFunctionName(containerName);

        // 传递jar包的base64编码
        String encode;
        try {
            encode = Base64.encode(new FileInputStream(jarLocation));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("找不到jar包", e);
        }
        Code code = new Code();
        code.setZipFile(encode);
        req.setCode(code);

        req.setDescription(createContainerDTO.getDescription());
        req.setTimeout(200L);
        req.setRuntime("Java8");
        req.setHandler("top.misec.BiliMain::mainHandler");

        JSONObject jsonConfig = JSONUtil.parseObj(createContainerDTO.getConfig());
        jsonConfig.set("dedeuserid", dedeuserid);
        jsonConfig.set("sessdata", sessdata);
        jsonConfig.set("biliJct", biliJct);

        Environment environment = new Environment();
        Variable v1 = new Variable();
        Variable v2 = new Variable();
        v1.setKey("scfFlag");
        v1.setValue("true");
        v2.setKey("config");
        v2.setValue(jsonConfig.toJSONString(0));
        Variable[] variables = new Variable[]{v1, v2};
        environment.setVariables(variables);
        req.setEnvironment(environment);

        try {
            CreateFunctionResponse resp = scfClient.CreateFunction(req);
            log.info("创建容器返回结果: {}", resp.getRequestId());
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("创建容器失败", e);
        }

        while (!created) {
            try {
                GetFunctionRequest getFunctionRequest = new GetFunctionRequest();
                getFunctionRequest.setFunctionName(containerName);
                GetFunctionResponse getFunctionResponse = scfClient.GetFunction(getFunctionRequest);
                String status = getFunctionResponse.getStatus();
                created = "Active".equals(status);
                if ("CreateFailed".equals(status)) {
                    removeContainer(containerName);
                    throw new RuntimeException("创建容器失败");
                }
            } catch (TencentCloudSDKException e) {
                throw new RuntimeException("获取容器详细信息失败", e);
            }
        }

        updateTrigger(containerName, "0 10 0 * * * *");

        bilibiliExecutor.execute(() -> init(containerName));

        // 获取用户B站数据
        return getContainerInfo(sessdata, dedeuserid);
    }

    private ContainerDTO getContainerInfo(String sessdata, String dedeuserid) {
        HttpCookie sessdataCookie = new HttpCookie("SESSDATA", sessdata);
        HttpCookie dedeUserID = new HttpCookie("DedeUserID", dedeuserid);
        sessdataCookie.setDomain(".bilibili.com");
        dedeUserID.setDomain(".bilibili.com");
        String body = HttpRequest.get("https://api.bilibili.com/x/web-interface/nav")
                .cookie(sessdataCookie)
                .execute().body();
        JSONObject data = JSONUtil.parseObj(body).getJSONObject("data");
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
                .dedeUserId(dedeuserid)
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
        com.tencentcloudapi.scf.v20180416.ScfClient scfClient = new ScfClient.Builder(apiConfig).build();
        // 绑定触发器
        try {
            CreateTriggerRequest createTriggerRequest = new CreateTriggerRequest();
            createTriggerRequest.setFunctionName(containerName);
            createTriggerRequest.setTriggerName(containerName + "-trigger");
            createTriggerRequest.setType("timer");
            createTriggerRequest.setTriggerDesc(cronExpression);
            CreateTriggerResponse createTriggerResponse = scfClient.CreateTrigger(createTriggerRequest);
            log.info("创建触发器返回结果: {}", JSONUtil.toJsonStr(createTriggerResponse.getTriggerInfo()));
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("创建触发器失败", e);
        }
    }

    @Override
    public void removeContainer(String containerName) {
        com.tencentcloudapi.scf.v20180416.ScfClient scfClient = new ScfClient.Builder(apiConfig).build();
        try {
            DeleteFunctionRequest deleteFunctionRequest = new DeleteFunctionRequest();
            deleteFunctionRequest.setFunctionName(containerName);
            scfClient.DeleteFunction(deleteFunctionRequest);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("删除容器失败", e);
        }
    }


    private void init(String containerName) {
        com.tencentcloudapi.scf.v20180416.ScfClient scfClient = new ScfClient.Builder(apiConfig).build();
        // 初次创建主动执行
        try {
            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.setFunctionName(containerName);
            InvokeResponse invokeResponse = scfClient.Invoke(invokeRequest);
            log.info("容器执行返回结果: {}", JSONUtil.toJsonStr(invokeResponse.getResult()));
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("执行容器失败", e);
        }
    }

    /**
     * 赛事预测
     * @param dedeuserid    B站用户ID
     * @param sessdata      cookie中的SESSDATA
     * @param biliJct       cookie中的bili_jct
     */
    private void predictGame(String dedeuserid, String sessdata, String biliJct) {

    }
}
