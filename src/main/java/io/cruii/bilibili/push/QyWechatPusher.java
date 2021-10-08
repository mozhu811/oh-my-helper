package io.cruii.bilibili.push;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
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
        HttpRequest httpRequest = HttpRequest.post("https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="
                + getAccessToken());
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


        httpRequest.body(requestBody.toJSONString(0));
        JSONObject resp = JSONUtil.parseObj(httpRequest.execute().body());
        if (resp.getInt("errcode") == 0) {
            log.info("推送成功");
            return true;
        } else {
            log.error("推送失败：{}", resp.getStr("errmsg"));
            return false;
        }
    }

    private String getAccessToken() {
        String resp = HttpRequest.get("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="
                + corpId + "&corpsecret=" + corpSecret).execute().body();

        return JSONUtil.parseObj(resp).getStr("access_token");
    }
}
