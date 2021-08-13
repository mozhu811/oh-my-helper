package io.cruii.bilibili.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.bilibili.vo.BilibiliUserVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.HttpCookie;

/**
 * @author cruii
 * Created on 2021/6/10
 */
@RestController
@RequestMapping("bilibili")
@Log4j2
public class BilibiliController {

    @GetMapping("{dedeuserid}")
    public BilibiliUserVO getBilibiliUser(@PathVariable Integer dedeuserid,
                                  @CookieValue String sessdata) {
        HttpCookie sessdataCookie = new HttpCookie("SESSDATA", sessdata);
        sessdataCookie.setDomain(".bilibili.com");
        String body = HttpRequest.get("https://api.bilibili.com/x/web-interface/nav")
                .cookie(sessdataCookie)
                .execute().body();
        JSONObject data = JSONUtil.parseObj(body).getJSONObject("data");
        Integer mid = data.getInt("mid");
        if (mid == null || !mid.equals(dedeuserid)) {
            throw new RuntimeException("获取B站信息异常");
        }
        InputStream avatarStream = HttpRequest.get(data.getStr("face"))
                .execute().bodyStream();
        return BilibiliUserVO.builder()
                .avatar("data:image/jpeg;base64," + Base64.encode(avatarStream))
                .username(data.getStr("uname"))
                .level(data.getJSONObject("level_info")
                        .getInt("current_level"))
                .build();
    }

    @GetMapping("qrCode")
    public String getLoginQrCode() {
        return null;
    }
}
