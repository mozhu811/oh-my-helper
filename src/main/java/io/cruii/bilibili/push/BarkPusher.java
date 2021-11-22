package io.cruii.bilibili.push;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;

/**
 * @author cruii
 * Created on 2021/10/14
 */
@Log4j2
public class BarkPusher implements Pusher{
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
        String res = HttpRequest.post("https://api.day.app/push").body(body.toJSONString(0)).execute().body();
        if (JSONUtil.isJson(res)) {
            JSONObject resp = JSONUtil.parseObj(res);
            if (resp.getInt("code") == 200) {
                log.info("Bark 推送成功");
                return true;
            }
        }
        log.error("Bark 推送失败: {}", body);
        return false;
    }
}
