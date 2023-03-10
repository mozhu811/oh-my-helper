package io.cruii.push.pusher.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.push.pusher.Pusher;
import lombok.extern.log4j.Log4j2;

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
    public boolean notifyExpired(String id) {
        return push("账号[" + id + "]登录失败，请访问 https://ohmyhelper.com/bilibili/ 重新扫码登陆更新Cookie");
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

        String resp = HttpUtil.post("https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+getAccessToken(), requestBody.toJSONString(0));
        JSONObject wxPushResp = JSONUtil.parseObj(resp);
        if (wxPushResp.getInt("errcode") == 0) {
            log.info("企业微信推送成功");
            return true;
        }
        log.error("企业微信推送失败: {}", wxPushResp);
        return false;
    }

    private String getAccessToken() {
        Map<String, Object> params = new HashMap<>();
        params.put("corpid", corpId);
        params.put("corpsecret", corpSecret);

        JSONObject accessTokenResp = JSONUtil.parseObj(HttpUtil.get("https://qyapi.weixin.qq.com/cgi-bin/gettoken", params));
        if (accessTokenResp.getInt("errcode") == 0) {
            return accessTokenResp.getStr("access_token");
        }
        throw new RuntimeException("获取企业微信access_token失败");
    }
}
