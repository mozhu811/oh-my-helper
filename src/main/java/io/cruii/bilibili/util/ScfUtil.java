package io.cruii.bilibili.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.scf.v20180416.ScfClient;
import com.tencentcloudapi.scf.v20180416.models.*;
import io.cruii.bilibili.config.TencentApiConfig;
import io.cruii.bilibili.dto.CreateContainerDTO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author cruii
 * Created on 2021/6/7
 */
public class ScfUtil {
    private ScfUtil(){}

    public static ListFunctionsResponse listFunctions(TencentApiConfig apiConfig) {
        try {
            ScfClient scfClient = buildClient(apiConfig);
            ListFunctionsRequest listFunctionsRequest = new ListFunctionsRequest();
            return scfClient.ListFunctions(listFunctionsRequest);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("获取容器列表失败", e);
        }
    }

    public static GetFunctionResponse getFunction(TencentApiConfig apiConfig, String functionName) {
        try {
            ScfClient scfClient = buildClient(apiConfig);
            GetFunctionRequest getFunctionRequest = new GetFunctionRequest();
            getFunctionRequest.setFunctionName(functionName);
            return scfClient.GetFunction(getFunctionRequest);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("获取容器详细信息失败", e);
        }
    }

    public static void createFunction(TencentApiConfig apiConfig,
                                      CreateContainerDTO createContainerDTO,
                                      String jarLocation) {

        ScfClient scfClient = buildClient(apiConfig);
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
            return scfClient.CreateFunction(req);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("创建容器失败", e);
        }
    }

    public static void createTrigger(TencentApiConfig apiConfig,
                                                      String containerName,
                                                      String cron) {
        ScfClient scfClient = buildClient(apiConfig);
        try {
            CreateTriggerRequest createTriggerRequest = new CreateTriggerRequest();
            createTriggerRequest.setFunctionName(containerName);
            createTriggerRequest.setTriggerName(containerName + "-trigger");
            createTriggerRequest.setType("timer");
            createTriggerRequest.setTriggerDesc(cron);
            scfClient.CreateTrigger(createTriggerRequest);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("创建触发器失败", e);
        }
    }

    public static void deleteFunction(TencentApiConfig apiConfig,
                                      String containerName) {
        ScfClient scfClient = buildClient(apiConfig);
        try {
            DeleteFunctionRequest deleteFunctionRequest = new DeleteFunctionRequest();
            deleteFunctionRequest.setFunctionName(containerName);
            scfClient.DeleteFunction(deleteFunctionRequest);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("删除容器失败", e);
        }
    }

    public static void executeFunction(TencentApiConfig apiConfig,
                             String containerName) {
        ScfClient scfClient = buildClient(apiConfig);
        // 初次创建主动执行
        try {
            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.setFunctionName(containerName);
            scfClient.Invoke(invokeRequest);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("执行容器失败", e);
        }
    }

    private static ScfClient buildClient(TencentApiConfig apiConfig) {
        Credential cred = new Credential(apiConfig.getSecretId(), apiConfig.getSecretKey());

        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(apiConfig.getScfEndpoint());

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        return new ScfClient(cred, apiConfig.getRegion(), clientProfile);
    }
}
