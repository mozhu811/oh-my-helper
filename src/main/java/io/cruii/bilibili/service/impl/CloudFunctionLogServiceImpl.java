package io.cruii.bilibili.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tencentcloudapi.cls.v20201016.ClsClient;
import com.tencentcloudapi.cls.v20201016.models.*;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.scf.v20180416.models.Function;
import com.tencentcloudapi.scf.v20180416.models.ListFunctionsRequest;
import com.tencentcloudapi.scf.v20180416.models.ListFunctionsResponse;
import io.cruii.bilibili.config.TencentApiConfig;
import io.cruii.bilibili.entity.CloudFunction;
import io.cruii.bilibili.entity.CloudFunctionLog;
import io.cruii.bilibili.mapper.CloudFunctionMapper;
import io.cruii.bilibili.service.CloudFunctionLogService;
import io.cruii.bilibili.util.ScfClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/6/7
 */
@Service
@Log4j2
public class CloudFunctionLogServiceImpl implements CloudFunctionLogService {
    static final List<String> IGNORE;

    static {
        IGNORE = Arrays.asList("-----版本信息-----",
                "当前版本: 1.0.0",
                "版本更新内容: 1.缩小jar包体积。",
                "2.以前投币都是从热榜获取，目前投币有一定几率会投给项目贡献者。",
                "3.修复日志等级，默认开启每日任务。",
                "最后更新日期: 2021-04-28",
                "项目开源地址: https://github.com/JunzhouLiu/BILIBILI-HELPER");
    }

    private final TencentApiConfig apiConfig;

    private final CloudFunctionMapper cloudFunctionMapper;

    public CloudFunctionLogServiceImpl(TencentApiConfig apiConfig,
                                       CloudFunctionMapper cloudFunctionMapper) {
        this.apiConfig = apiConfig;
        this.cloudFunctionMapper = cloudFunctionMapper;
    }

    @Override
    public List<CloudFunctionLog> listLogs(String dedeuserid, long startTime, long endTime) throws TencentCloudSDKException {
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
        String regex = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.*";

        return jsonObjects.stream()
                .filter(obj -> CharSequenceUtil.isNotBlank(obj.getStr("SCF_Message")))
                .filter(obj -> !IGNORE.contains(obj.getStr("SCF_Message").trim()))
                .map(obj -> {
                            Instant instant = Instant.ofEpochMilli(obj.getLong("SCF_LogTime") / 1000000);
                            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                            String message = obj.getStr("SCF_Message").trim();
                            if (Pattern.compile(regex).matcher(message).matches()) {
                                String[] split = message.split(":");
                                if (split.length < 6) {
                                    message = split[split.length - 1].trim();
                                } else {
                                    message = "^_^";
                                }
                            }
                            CloudFunctionLog cloudFunctionLog = new CloudFunctionLog();
                            cloudFunctionLog.setLogTime(localDateTime);
                            cloudFunctionLog.setLevel(obj.getStr("SCF_Level"));
                            cloudFunctionLog.setMessage(message);
                            return cloudFunctionLog;
                        }
                ).collect(Collectors.toList());
    }

    Function getFunction(String functionName) throws TencentCloudSDKException {

        ListFunctionsRequest req = new ListFunctionsRequest();
        req.setSearchKey(functionName);
        com.tencentcloudapi.scf.v20180416.ScfClient scfClient = new ScfClient.Builder(apiConfig).build();

        ListFunctionsResponse listFunctionsResponse = scfClient.ListFunctions(req);
        return listFunctionsResponse.getFunctions().length > 0 ? listFunctionsResponse.getFunctions()[0] : null;
    }

    String getTopicId() throws TencentCloudSDKException {

        DescribeTopicsRequest req = new DescribeTopicsRequest();

        DescribeTopicsResponse resp = buildClsClient().DescribeTopics(req);

        return resp.getTopics()[0].getTopicId();
    }

    SearchLogResponse getLog(long from, long to, String context, String functionName, String topicId) throws TencentCloudSDKException {
        log.debug("{},{},{},{},{}", from, to, context, functionName, topicId);
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
        clientProfile.setDebug(true);
        return new ClsClient(cred, apiConfig.getRegion(), clientProfile);
    }

}
