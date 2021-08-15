package io.cruii.bilibili.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.zxing.WriterException;
import io.cruii.bilibili.constant.BilibiliAPI;
import io.cruii.bilibili.util.QrCodeGenerator;
import io.cruii.bilibili.vo.BilibiliLoginVO;
import io.cruii.bilibili.vo.BilibiliUserVO;
import io.cruii.bilibili.vo.QrCodeVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    @GetMapping("user")
    public BilibiliUserVO getBilibiliUser(@RequestParam Integer dedeuserid,
                                          @RequestParam String sessdata) {
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
    public QrCodeVO getLoginQrCode() throws IOException, WriterException {
        HttpResponse response = HttpRequest.get(BilibiliAPI.GET_QR_CODE_LOGIN_URL).execute();
        if (response.getStatus() == 200) {
            JSONObject jsonBody = JSONUtil.parseObj(response.body());
            if (jsonBody.getInt("code") == 0 && Boolean.TRUE.equals(jsonBody.getBool("status"))) {
                JSONObject data = jsonBody.getJSONObject("data");
                String qrCodeUrl = data.getStr("url");
                String oauthKey = data.getStr("oauthKey");

                QrCodeVO qrCodeVO = new QrCodeVO();
                qrCodeVO.setQrCodeUrl(qrCodeUrl);
                qrCodeVO.setOauthKey(oauthKey);

                byte[] bytes = QrCodeGenerator.generateQrCode(qrCodeUrl, 180, true);
                qrCodeVO.setQrCodeImg("data:image/png;base64," + Base64.encode(bytes));
                return qrCodeVO;
            }
        }
        throw new RuntimeException("获取B站二维码登录链接异常");
    }

    @GetMapping("login")
    public BilibiliLoginVO getCookie(@RequestParam String oauthKey) {
        HttpResponse response = HttpRequest.post(BilibiliAPI.GET_QR_CODE_LOGIN_INFO_URL).body("oauthKey=" + oauthKey).execute();
        /*
        status: false
        data:
        -2 = oauthKey过期
        -4 = 未扫码
        -5 = 扫码未确认

        status: true
        直接从Response的Header中获取cookie
         */

        JSONObject bodyJson = JSONUtil.parseObj(response.body());
        Boolean status = bodyJson.getBool("status");
        BilibiliLoginVO bilibiliLoginVO = new BilibiliLoginVO();
        if (Boolean.TRUE.equals(status)) {
            String biliJct = response.getCookieValue("bili_jct");
            String dedeuserid = response.getCookieValue("DedeUserID");
            String sessdata = response.getCookieValue("SESSDATA").replace("%2C", ",").replace("%2A", "*");

            bilibiliLoginVO.setBiliJct(biliJct);
            bilibiliLoginVO.setDedeuserid(Integer.parseInt(dedeuserid));
            bilibiliLoginVO.setSessdata(sessdata);
            bilibiliLoginVO.setCode(0);

            return bilibiliLoginVO;
        }

        bilibiliLoginVO.setCode(bodyJson.getInt("data"));
        return bilibiliLoginVO;
    }
}
