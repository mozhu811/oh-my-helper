package io.cruii.bilibili.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tencentcloudapi.cls.v20201016.ClsClient;
import com.tencentcloudapi.cls.v20201016.models.LogInfo;
import com.tencentcloudapi.cls.v20201016.models.SearchLogRequest;
import com.tencentcloudapi.cls.v20201016.models.SearchLogResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import io.cruii.bilibili.config.TencentApiConfig;
import io.cruii.bilibili.entity.CloudFunction;
import io.cruii.bilibili.mapper.CloudFunctionMapper;
import io.cruii.bilibili.service.ContainerLogService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/6/7
 */
@Service
@Log4j2
public class ContainerLogServiceImpl implements ContainerLogService {
    static final List<String> IGNORE;

    static {
        IGNORE = Arrays.asList("版本信息",
                "当前版本",
                "版本更新内容",
                "最后更新日期",
                "项目开源地址");
    }

    private final TencentApiConfig apiConfig;

    private final CloudFunctionMapper cloudFunctionMapper;

    public ContainerLogServiceImpl(TencentApiConfig apiConfig,
                                   CloudFunctionMapper cloudFunctionMapper) {
        this.apiConfig = apiConfig;
        this.cloudFunctionMapper = cloudFunctionMapper;
    }

    @Override
    public List<String> listLogs(String dedeuserid, long startTime, long endTime) throws TencentCloudSDKException {
        // 从数据库获取对应的functionName
        LambdaQueryWrapper<CloudFunction> lambdaQueryWrapper = Wrappers.lambdaQuery(CloudFunction.class).eq(CloudFunction::getDedeuserid, dedeuserid);
        CloudFunction cloudFunction = cloudFunctionMapper.selectOne(lambdaQueryWrapper);
        if (cloudFunction == null) {
            return CollUtil.newArrayList();
        }

        // 获取函数投递的日志的topicId
        String topicId = cloudFunction.getTopicId();

        String context = null;
        List<JSONObject> jsonObjects = new ArrayList<>();
        while (!"".equals(context)) {
            SearchLogResponse logResponse = getLog(startTime, endTime, context, cloudFunction.getFunctionName(), topicId);

            jsonObjects
                    .addAll(Arrays.stream(logResponse.getResults())
                            .map(LogInfo::getLogJson)
                            .map(JSONUtil::parseObj)
                            .collect(Collectors.toList()));
            context = logResponse.getContext();
        }

        List<String> scfMessage = jsonObjects.stream()
                .map(o -> o.getStr("SCF_Message").trim())
                .filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        int index = 0;
        for (int i = 0; i < scfMessage.size(); i++) {
            if (scfMessage.get(i).contains("DEBUG : 任务启动中")){
                index = i;
                break;
            }
        }
        return scfMessage.subList(index, scfMessage.size() - 1);
    }

    SearchLogResponse getLog(long from, long to, String context, String functionName, String topicId) throws TencentCloudSDKException {
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
