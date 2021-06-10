package io.cruii.bilibili.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tencentcloudapi.cls.v20201016.ClsClient;
import com.tencentcloudapi.cls.v20201016.models.LogInfo;
import com.tencentcloudapi.cls.v20201016.models.SearchLogRequest;
import com.tencentcloudapi.cls.v20201016.models.SearchLogResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.scf.v20180416.models.Function;
import com.tencentcloudapi.scf.v20180416.models.ListFunctionsRequest;
import com.tencentcloudapi.scf.v20180416.models.ListFunctionsResponse;
import io.cruii.bilibili.config.TencentApiConfig;
import io.cruii.bilibili.service.ContainerLogService;
import io.cruii.bilibili.util.ScfClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/6/7
 */
@Service
@Log4j2
public class ContainerLogServiceImpl implements ContainerLogService {

    private final TencentApiConfig apiConfig;

    public ContainerLogServiceImpl(TencentApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    @Override
    public List<String> listLogs(String dedeuserid, long startTime, long endTime) {
        com.tencentcloudapi.scf.v20180416.ScfClient scfClient = new ScfClient.Builder(apiConfig).build();
        ListFunctionsRequest req = new ListFunctionsRequest();
        ListFunctionsResponse listFunctionsResponse;
        try {
            listFunctionsResponse = scfClient.ListFunctions(req);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("获取容器列表失败", e);
        }
        Optional<Function> function = Arrays.stream(listFunctionsResponse.getFunctions())
                .filter(f -> f.getDescription().equals(dedeuserid)).findFirst();
        if (!function.isPresent()) {
            throw new RuntimeException("该用户未拥有容器");
        }

        String context = null;
        List<JSONObject> jsonObjects = new ArrayList<>();
        while (!"".equals(context)) {
            SearchLogResponse logResponse;
            try {
                logResponse = getLog(startTime, endTime, context,
                        function.get().getFunctionName(), apiConfig.getClsTopicId());
                jsonObjects
                        .addAll(Arrays.stream(logResponse.getResults())
                                .map(LogInfo::getLogJson)
                                .map(JSONUtil::parseObj)
                                .collect(Collectors.toList()));
                context = logResponse.getContext();
            } catch (TencentCloudSDKException e) {
                throw new RuntimeException("获取容器日志失败", e);
            }
        }

        return jsonObjects.stream()
                .map(o -> o.getStr("SCF_Message").trim())
                .filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
    }

    private SearchLogResponse getLog(long from, long to, String context, String functionName, String topicId) throws TencentCloudSDKException {
        SearchLogRequest req = new SearchLogRequest();
        req.setTopicId(topicId);
        req.setFrom(from);
        req.setTo(to);
        req.setContext(context);
        req.setQuery("SCF_FunctionName:" + functionName);
        req.setSort("asc");

        return buildClsClient().SearchLog(req);
    }

    private ClsClient buildClsClient() {
        Credential cred = new Credential(apiConfig.getSecretId(), apiConfig.getSecretKey());

        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(apiConfig.getClsEndpoint());

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new ClsClient(cred, apiConfig.getRegion(), clientProfile);
    }

}
