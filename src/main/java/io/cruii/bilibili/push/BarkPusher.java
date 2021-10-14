package io.cruii.bilibili.push;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author cruii
 * Created on 2021/10/14
 */
@Log4j2
public class BarkPusher implements Pusher{
    private final String token;

    public BarkPusher(String token) {
        this.token = token;
    }

    @Override
    public boolean push(String content) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.day.app/{token}/{title}/{content}")
                .build(token, "Bilibili Helper Hub任务日志", content)
                .toString();
        JSONObject resp = JSONUtil.parseObj(HttpRequest.get(url).execute().body());
        return resp.getInt("code") == 200;
    }
}
