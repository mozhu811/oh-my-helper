package io.cruii.bilibili.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tencentcloudapi.cls.v20201016.ClsClient;
import com.tencentcloudapi.cls.v20201016.models.*;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.scf.v20180416.ScfClient;
import com.tencentcloudapi.scf.v20180416.models.Function;
import com.tencentcloudapi.scf.v20180416.models.ListFunctionsRequest;
import com.tencentcloudapi.scf.v20180416.models.ListFunctionsResponse;
import io.cruii.bilibili.config.TencentApiConfig;
import io.cruii.bilibili.dto.BilibiliUserDTO;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.CloudFunctionLog;
import io.cruii.bilibili.mapper.BilibiliUserMapper;
import io.cruii.bilibili.service.CloudFunctionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
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
 * Created on 2021/6/6
 */
@Service
@Log4j2
public class CloudFunctionServiceImpl implements CloudFunctionService {
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

    private final BilibiliUserMapper bilibiliUserMapper;

    public CloudFunctionServiceImpl(TencentApiConfig apiConfig, BilibiliUserMapper bilibiliUserMapper) {
        this.apiConfig = apiConfig;
        this.bilibiliUserMapper = bilibiliUserMapper;
    }

    @Override
    public List<CloudFunctionLog> listLogs(String username, long startTime, long endTime) throws TencentCloudSDKException {
        Function function = getFunction(username);
        if (function == null) {
            return CollUtil.newArrayList();
        }

        String topicId = getTopicId();

        String context = null;
        List<JSONObject> jsonObjects = new ArrayList<>();
        while (!"".equals(context)) {
            SearchLogResponse logResponse = getLog(startTime, endTime, context, function.getFunctionName(), topicId);

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

    @Override
    public List<BilibiliUserDTO> listFunctions() {
        List<BilibiliUser> bilibiliUsers = bilibiliUserMapper.selectList(null);

        return bilibiliUsers.stream()
                .map(b -> {
                    String body = HttpRequest.get("https://api.bilibili.com/x/space/myinfo")
                            .cookie("SESSDATA=" + b.getSessdata() + "; Path=/;")
                            .execute().body();
                    JSONObject data = JSONUtil.parseObj(body).getJSONObject("data");
                    InputStream avatarStream = HttpRequest.get(data.getStr("face"))
                            .execute().bodyStream();
                    StringBuilder sb = new StringBuilder();
                    String username = data.getStr("name");
                    for (int i = 0; i < username.length(); i++) {
                        if (i > 0 && i < username.length() - 1) {
                            sb.append("*");
                        } else {
                            sb.append(username.charAt(i));
                        }
                    }
                    return BilibiliUserDTO.builder()
                            .username(sb.toString())
                            .avatar("data:image/jpeg;base64," + Base64.encode(avatarStream))
                            .coins(data.getDouble("coins"))
                            .level(data.getInt("level"))
                            .vipType(data.getJSONObject("vip").getInt("type")).build();
                }).collect(Collectors.toList());
    }

    Function getFunction(String username) throws TencentCloudSDKException {

        ListFunctionsRequest req = new ListFunctionsRequest();
        req.setSearchKey(username);

        ListFunctionsResponse listFunctionsResponse = buildScfClient().ListFunctions(req);
        log.info(JSONUtil.toJsonStr(listFunctionsResponse));
        return listFunctionsResponse.getFunctions().length > 0 ? listFunctionsResponse.getFunctions()[0] : null;
    }

    String getTopicId() throws TencentCloudSDKException {

        DescribeTopicsRequest req = new DescribeTopicsRequest();

        DescribeTopicsResponse resp = buildClsClient().DescribeTopics(req);

        return resp.getTopics()[0].getTopicId();
    }

    SearchLogResponse getLog(long from, long to, String context, String functionName, String topicId) throws TencentCloudSDKException {

        SearchLogRequest req = new SearchLogRequest();
        req.setTopicId(topicId);
        req.setFrom(from);
        req.setTo(to);
        req.setContext(context);
        req.setQuery(functionName);
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

    private ScfClient buildScfClient() {
        Credential cred = new Credential(apiConfig.getSecretId(), apiConfig.getSecretKey());

        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(apiConfig.getScfEndpoint());

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        return new ScfClient(cred, apiConfig.getRegion(), clientProfile);
    }
}
