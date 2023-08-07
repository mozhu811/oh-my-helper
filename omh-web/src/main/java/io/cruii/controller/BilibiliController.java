package io.cruii.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.zxing.WriterException;
import io.cruii.component.BilibiliDelegate;
import io.cruii.constant.BilibiliAPI;
import io.cruii.model.BiliQrCodeStatus;
import io.cruii.model.BiliQrcode;
import io.cruii.pojo.vo.BiliLoginVO;
import io.cruii.pojo.vo.BiliTaskUserVO;
import io.cruii.pojo.vo.OmhUserVO;
import io.cruii.pojo.vo.QrCodeVO;
import io.cruii.service.BilibiliUserService;
import io.cruii.service.TaskConfigService;
import io.cruii.util.CosUtil;
import io.cruii.util.QrCodeGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cruii
 * Created on 2021/6/10
 */
@RestController
@RequestMapping("bilibili")
@Log4j2
public class BilibiliController {

    private final BilibiliUserService userService;
    private final TaskConfigService taskConfigService;

    public BilibiliController(BilibiliUserService userService,
                              TaskConfigService taskConfigService) {
        this.userService = userService;
        this.taskConfigService = taskConfigService;
    }

    @GetMapping("user")
    public OmhUserVO getBilibiliUser(@CookieValue String dedeuserid,
                                     @CookieValue String sessdata,
                                     @CookieValue String biliJct) {
        return userService.getUser(dedeuserid, sessdata, biliJct);
    }

    @GetMapping("users")
    public Page<BiliTaskUserVO> listUsers(@RequestParam Integer page, @RequestParam Integer size) {
        if (page <= 0) {
            page = 1;
        }

        if (size < 24) {
            size = 24;
        }
        return userService.list(page, size);
    }

    @GetMapping("qrCode")
    public QrCodeVO getLoginQrCode() {
        try (HttpResponse response = HttpRequest.get(BilibiliAPI.GET_QR_CODE_LOGIN_URL).execute()) {
            if (response.getStatus() == 200) {
                BiliQrcode qrcode = JSONUtil.parseObj(response.body()).getJSONObject("data").toBean(BiliQrcode.class);
                String qrCodeUrl = qrcode.getUrl();
                String qrCodeKey = qrcode.getQrcodeKey();

                QrCodeVO qrCodeVO = new QrCodeVO();
                qrCodeVO.setQrCodeUrl(qrCodeUrl);
                qrCodeVO.setQrCodeKey(qrCodeKey);

                byte[] bytes = QrCodeGenerator.generateQrCode(qrCodeUrl, 180, true);
                qrCodeVO.setQrCodeImg("data:image/png;base64," + Base64.encode(bytes));
                return qrCodeVO;
            }
        } catch (WriterException | IOException e) {
            throw new RuntimeException("获取B站二维码登录链接异常", e);
        }
        throw new RuntimeException("获取B站二维码登录链接异常");
    }

    @GetMapping("login")
    public BiliLoginVO login(@RequestParam String qrCodeKey) throws MalformedURLException {
        try (HttpResponse response = HttpRequest.get(BilibiliAPI.GET_QR_CODE_LOGIN_INFO_URL).body("qrcode_key=" + qrCodeKey).execute()) {
        /*
        当密钥正确时但未扫描时code为86101
        扫描成功但手机端未确认时code为86090
        扫描成功手机端确认登录后，code为0，并向浏览器写入cookie
        二维码失效时code为86038
         */
            BiliQrCodeStatus qrCodeStatus = JSONUtil.parseObj(response.body())
                    .getJSONObject("data")
                    .toBean(BiliQrCodeStatus.class);
            int code = qrCodeStatus.getCode();

            BiliLoginVO biliLoginVO = new BiliLoginVO();
            if (code == 0) {
                String decodeUrl = URLUtil.decode(qrCodeStatus.getUrl());
                Map<String, Object> urlParams = getUrlParams(new URL(decodeUrl).getQuery());
                // 将sessdata中的,和*替换为%2C和%2A
                final String sessdata = ((String) urlParams.get("SESSDATA")).replace(",", "%2C").replace("*", "%2A");
                final String biliJct = response.getCookieValue("bili_jct");
                final String dedeuserid = response.getCookieValue("DedeUserID");

                new Thread(() -> {
                    userService.save(dedeuserid, sessdata, biliJct);
                    taskConfigService.updateCookie(dedeuserid, sessdata, biliJct);
                }).start();


                try {
                    BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct, false);
                    String avatar = delegate.getUserDetails().getFace();
                    if (avatar != null) {
                        CosUtil.upload(avatar, dedeuserid);
                    }
                } catch (Exception e) {
                    log.error("Download user avatar failed. {}", e.getMessage());
                }

                biliLoginVO.setBiliJct(biliJct);
                biliLoginVO.setDedeuserid(dedeuserid);
                biliLoginVO.setSessdata(sessdata);
                biliLoginVO.setCode(0);

                return biliLoginVO;
            }

            biliLoginVO.setCode(code);
            return biliLoginVO;
        }
    }

    private static Map<String, Object> getUrlParams(String param) {
        Map<String, Object> map = new HashMap<>(0);
        if (param.isEmpty()) {
            return map;
        }
        String[] params = param.split("&");
        for (String s : params) {
            String[] p = s.split("=");
            if (p.length == 2) {
                map.put(p[0], p[1]);
            }
        }
        return map;
    }
}
