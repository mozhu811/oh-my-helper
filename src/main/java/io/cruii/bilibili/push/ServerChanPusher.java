package io.cruii.bilibili.push;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author cruii
 * Created on 2021/10/03
 */
@Log4j2
public class ServerChanPusher implements Pusher {
    private final String scKey;

    public ServerChanPusher(String scKey) {
        this.scKey = scKey;
    }

    @Override
    public boolean push(String content) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://sctapi.ftqq.com/{scKey}.send?title={title}&desp={content}")
                .build(scKey, "Bilibili Helper Hub任务日志", content.replace("\n", "\n\n")).toString();
        String body = HttpRequest.post(URLUtil.encode(url)).execute().body();
        log.info(body);
        if (JSONUtil.isJson(body)) {
            JSONObject resp = JSONUtil.parseObj(body);
            if (resp.getInt("code") == 0) {
                log.info("ServerChan推送成功");
                return true;
            }
        }
        log.error("ServerChan推送失败：{}", body);
        return false;
    }
}
