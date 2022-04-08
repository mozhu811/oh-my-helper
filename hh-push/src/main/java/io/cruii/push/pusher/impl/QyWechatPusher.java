package io.cruii.push.pusher.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.push.pusher.Pusher;
import io.cruii.util.HttpUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/9/26
 */
@Log4j2
public class QyWechatPusher implements Pusher {
    private final String corpId;
    private final String corpSecret;
    private final String agentId;
    private final String mediaId;

    public QyWechatPusher(String corpId, String corpSecret, String agentId, String mediaId) {
        this.corpId = corpId;
        this.corpSecret = corpSecret;
        this.agentId = agentId;
        this.mediaId = mediaId;
    }

    @Override
    public boolean push(String content) {
        JSONObject requestBody = JSONUtil.createObj();
        requestBody.set("touser", "@all")
                .set("msgtype", "mpnews")
                .set("agentid", agentId)
                .set("mpnews", JSONUtil.createObj()
                        .set("articles", JSONUtil.createArray()
                                .put(JSONUtil.createObj().set("title", "Bilibili Helper Hub任务日志")
                                        .set("thumb_media_id", mediaId)
                                        .set("author", "Bilibili Helper Hub")
                                        .set("content", content)
                                        .set("digest", Arrays.stream(content.split("<br>"))
                                                .skip(content.split("<br>").length - 3L)
                                                .collect(Collectors.joining("\n")) + "\n\n点击查看详细日志"))));


        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        URI uri = HttpUtil.buildUri("https://qyapi.weixin.qq.com/cgi-bin/message/send", params);
        StringEntity entity = new StringEntity(requestBody.toJSONString(0), "UTF-8");
        entity.setContentType("application/json");
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(entity);

        try (CloseableHttpResponse httpResponse = HttpUtil.buildHttpClient().execute(httpPost)) {
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                log.info("企业微信推送成功");
                EntityUtils.consume(httpResponse.getEntity());
                return true;
            }
            log.error("企业微信推送失败: {}", EntityUtils.toString(httpResponse.getEntity()));
            EntityUtils.consume(httpResponse.getEntity());
        } catch (Exception e) {
            log.error("企业微信推送失败", e);
        }
        return false;
    }

    private String getAccessToken() {
        Map<String, String> params = new HashMap<>();
        params.put("corpid", corpId);
        params.put("corpsecret", corpSecret);
        URI uri = HttpUtil.buildUri("https://qyapi.weixin.qq.com/cgi-bin/gettoken", params);
        HttpGet httpGet = new HttpGet(uri);

        try (CloseableHttpResponse httpResponse = HttpUtil.buildHttpClient().execute(httpGet)) {
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String body = EntityUtils.toString(httpResponse.getEntity());
                String accessToken = JSONUtil.parseObj(body).getStr("access_token");
                EntityUtils.consume(httpResponse.getEntity());
                return accessToken;
            }
        } catch (Exception e) {
            log.error("获取企业微信access_token失败", e);
        }
        throw new RuntimeException("获取企业微信access_token失败");
    }
}
