package io.cruii.push.pusher.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.push.pusher.Pusher;
import io.cruii.util.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FeiShuCustomBotPusher implements Pusher {
    private final String feiShuCustomBot;

    public FeiShuCustomBotPusher(String feiShuCustomBot) {
        this.feiShuCustomBot = feiShuCustomBot;
    }

    @Override
    public boolean notifyExpired(String id) {
        JSONArray elements = JSONUtil.createArray()
                .set(JSONUtil.createObj().set("tag", "div")
                        .set("text", JSONUtil.createObj()
                                .set("tag", "lark_md")
                                .set("content", "账号[" + id + "]登录失败，请访问 https://ohmyhelper.com/bilibili/ 重新扫码登陆更新Cookie"))
                        .set("extra", JSONUtil.createObj()
                                .set("tag", "button")
                                .set("text", JSONUtil.createObj()
                                        .set("tag", "lark_md")
                                        .set("content", "立即登录"))
                                .set("type", "primary")
                                .set("multi_url", JSONUtil.createObj()
                                        .set("url", "https://ohmyhelper.com/login")
                                        .set("pc_url", "")
                                        .set("android_url", "")
                                        .set("ios_url", ""))));
        JSONObject cardLink = JSONUtil.createObj().set("url", "")
                .set("pc_url", "")
                .set("android_url", "")
                .set("ios_url", "");
        return push0(elements, cardLink);
    }

    @Override
    public boolean push(String content) {
        JSONArray elements = JSONUtil.createArray()
                .set(JSONUtil.createObj().set("tag", "div")
                        .set("text", JSONUtil.createObj()
                                .set("tag", "lark_md")
                                .set("content", content)));
        JSONObject cardLink = JSONUtil.createObj().set("url", "")
                .set("pc_url", "")
                .set("android_url", "")
                .set("ios_url", "");
        return push0(elements, cardLink);
    }

    private boolean push0(JSONArray elements, JSONObject cardLink) {
        JSONObject config = JSONUtil.createObj().set("wide_screen_mode", true);
        JSONObject header = JSONUtil.createObj().set("template", "yellow")
                .set("title", JSONUtil.createObj().set("tag", "plain_text").set("content", "OH MY HELPER 消息推送"));

        JSONObject card = JSONUtil.createObj()
                .set("config", config)
                .set("header", header)
                .set("elements", elements)
                .set("card_link", cardLink);

        JSONObject requestBody = JSONUtil.createObj();
        requestBody.set("msg_type", "interactive")
                .set("card", card);

        Request request = new Request.Builder()
                .url(feiShuCustomBot)
                .post(RequestBody.create(requestBody.toJSONString(0).getBytes(StandardCharsets.UTF_8),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = OkHttpUtil.executeWithRetry(request, false)) {
            if (response.isSuccessful()) {
                log.info("飞书自定义机器人推送成功");
                return true;
            } else {
                log.error("飞书自定义机器人推送失败: {}", response.body().string());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
