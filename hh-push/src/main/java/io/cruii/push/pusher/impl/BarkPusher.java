package io.cruii.push.pusher.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.push.pusher.Pusher;
import io.cruii.util.HttpUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.net.URI;

/**
 * @author cruii
 * Created on 2021/10/14
 */
@Log4j2
public class BarkPusher implements Pusher {
    private final String deviceKey;

    public BarkPusher(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    @Override
    public boolean push(String content) {
        JSONObject body = JSONUtil.createObj();
        body.set("body", content)
                .set("device_key", deviceKey)
                .set("title", "Bilibili Helper Hub任务日志");
        URI uri = HttpUtil.buildUri("https://api.day.app/push", null);
        HttpPost httpPost = new HttpPost(uri);
        StringEntity stringEntity = new StringEntity(body.toJSONString(0), "utf-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);

        try (CloseableHttpResponse httpResponse = HttpUtil.buildHttpClient().execute(httpPost)) {
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                log.info("Bark推送成功");
                EntityUtils.consume(httpResponse.getEntity());
                return true;
            }
            log.error("Bark推送失败: {}", EntityUtils.toString(httpResponse.getEntity()));
            EntityUtils.consume(httpResponse.getEntity());
        } catch (Exception e) {
            log.error("Bark推送失败", e);
        }
        return false;
    }
}
