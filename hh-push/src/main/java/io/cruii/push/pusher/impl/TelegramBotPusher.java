package io.cruii.push.pusher.impl;

import io.cruii.push.pusher.Pusher;
import io.cruii.util.HttpUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cruii
 * Created on 2021/10/03
 */
@Log4j2
public class TelegramBotPusher implements Pusher {
    private final String token;

    private final String chatId;

    public TelegramBotPusher(String token, String chatId) {
        this.token = token;
        this.chatId = chatId;
    }

    @Override
    public boolean notifyExpired(String id) {
        return push("账号[" + id + "]登录失败，请访问 https://ohmyhelper.com/bilibili/ 重新扫码登陆更新Cookie");
    }

    @Override
    public boolean push(String content) {
        URI uri = HttpUtil.buildUri("https://api.telegram.org/bot" + token + "/sendMessage");
        List<NameValuePair> formData = new ArrayList<>();
        formData.add(new BasicNameValuePair("chat_id", chatId));
        formData.add(new BasicNameValuePair("text", content));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8);
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpUtil.buildHttpClient();
             CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                log.info("Telegram推送成功");
                EntityUtils.consume(httpResponse.getEntity());
                return true;
            }
            log.error("Telegram推送失败: {}", EntityUtils.toString(httpResponse.getEntity()));
            EntityUtils.consume(httpResponse.getEntity());
        } catch (Exception e) {
            log.error("Telegram推送失败", e);
        }
        return false;
    }
}
