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
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Service
@Log4j2
public class ContainerServiceImpl implements ContainerService {

    private final TencentApiConfig apiConfig;


    @Value("${scf.jar-location}")
    private String jarLocation;

    public ContainerServiceImpl(TencentApiConfig apiConfig) {
        this.apiConfig = apiConfig;
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
        String dedeuserid = createContainerDTO.getDedeuserid();
        String sessdata = createContainerDTO.getSessdata();
        String biliJct = createContainerDTO.getBiliJct();

        // 构建ScfClient
        com.tencentcloudapi.scf.v20180416.ScfClient scfClient = new ScfClient.Builder(apiConfig).build();
        CreateFunctionRequest req = new CreateFunctionRequest();

        // 设置请求参数
        req.setFunctionName(createContainerDTO.getContainerName());

        // 传递jar包的base64编码
        String encode = Base64.encode(new FileInputStream(jarLocation));
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
            log.info(resp.getRequestId());
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("创建容器失败", e);
        }

        // 获取用户B站数据
        return getContainerInfo(sessdata, dedeuserid);
    }

    private ContainerDTO getContainerInfo(String sessdata, String dedeuserid) {
        HttpCookie sessdataCookie = new HttpCookie("SESSDATA", sessdata);
        HttpCookie dedeUserID = new HttpCookie("DedeUserID", dedeuserid);
        sessdataCookie.setDomain("" +
                ".bilibili.com");
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
        Double coins = JSONUtil.parseObj(coinResp).getJSONObject("data").getDouble("money");
        JSONObject vip = data.getJSONObject("vip");
        JSONObject levelInfo = data.getJSONObject("level_info");
        return ContainerDTO.builder()
                .dedeUserId(dedeuserid)
                .username(sb.toString())
                .avatar("data:image/jpeg;base64," + Base64.encode(avatarStream))
                .coins(coins)
                .level(levelInfo.getInt("current_level"))
                .currentExp(levelInfo.getInt("current_exp"))
                .nextExp(levelInfo.getInt("next_exp"))
                .vipType(vip.getInt("type"))
                .dueDate(vip.getLong("due_date"))
                .key(SecureUtil.md5(sessdata)).build();
    }
}
