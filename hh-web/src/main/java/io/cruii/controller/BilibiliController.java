package io.cruii.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.zxing.WriterException;
import io.cruii.component.BilibiliDelegate;
import io.cruii.constant.BilibiliAPI;
import io.cruii.exception.BilibiliCookieExpiredException;
import io.cruii.exception.BilibiliUserNotFoundException;
import io.cruii.model.BiliQrcode;
import io.cruii.pojo.dto.BiliTaskUserDTO;
import io.cruii.pojo.vo.BiliLoginVO;
import io.cruii.pojo.vo.BiliTaskUserVO;
import io.cruii.pojo.vo.OmhUserVO;
import io.cruii.pojo.vo.QrCodeVO;
import io.cruii.service.BilibiliUserService;
import io.cruii.service.TaskConfigService;
import io.cruii.util.OkHttpUtil;
import io.cruii.util.QrCodeGenerator;
import lombok.extern.log4j.Log4j2;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpCookie;
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

    @GetMapping("user")
    public OmhUserVO getBilibiliUser(@RequestParam String dedeuserid,
                                     @RequestParam String sessdata) {
        BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, null);
        delegate.getSpaceAccInfo(dedeuserid);
        HttpCookie sessdataCookie = new HttpCookie("SESSDATA", sessdata);
        sessdataCookie.setDomain(".bilibili.com");
        JSONObject data;
        try (HttpResponse response = HttpRequest.get(BilibiliAPI.GET_USER_INFO_NAV).cookie(sessdataCookie).execute()) {
            String body = response.body();
            JSONObject resp = JSONUtil.parseObj(body);
            if (resp.getInt("code") == -101) {
                throw new BilibiliCookieExpiredException(dedeuserid);
            }
            data = resp.getJSONObject("data");
        }

        String mid = data.getStr("mid");

        // 找不到用户
        if (mid == null || !mid.equals(dedeuserid)) {
            throw new BilibiliUserNotFoundException(dedeuserid);
        }
        return new OmhUserVO()
                .setUserId(mid)
                .setNickname(data.getStr("uname"))
                .setBiliTaskConfigId(taskConfigService.getTask(dedeuserid).getId());
    }

    @PutMapping("user")
    @ResponseStatus(HttpStatus.CREATED)
    public void updateUser(@RequestBody BiliTaskUserDTO biliTaskUserDTO) {
        userService.save(biliTaskUserDTO);
    }

    @GetMapping("users")
    public Page<BiliTaskUserVO> listUsers(@RequestParam Integer page, @RequestParam Integer size) {
        if (page <= 0) {
            page = 1;
        }

        if (size < 30) {
            size = 30;
        }
        return userService.list(page, size);
    }

    @GetMapping("qrCode")
    public QrCodeVO getLoginQrCode() {
        Request request = new Request.Builder()
                .url(BilibiliAPI.GET_QR_CODE_LOGIN_URL)
                .get().build();

        try (Response qrCodeResponse = OkHttpUtil.executeWithRetry(request)){
            if (qrCodeResponse.isSuccessful()) {
                okhttp3.ResponseBody body = qrCodeResponse.body();
                assert body != null;
                BiliQrcode biliQrcode = JSONUtil.parseObj(body.string()).getJSONObject("data")
                        .toBean(BiliQrcode.class);
                byte[] qrcodeBytes = QrCodeGenerator.generateQrCode(biliQrcode.getUrl(), 180, true);
                QrCodeVO qrCodeVO = new QrCodeVO();
                qrCodeVO.setQrCodeUrl(biliQrcode.getUrl());
                qrCodeVO.setQrCodeKey(biliQrcode.getQrcodeKey());
                qrCodeVO.setQrCodeImg("data:image/png;base64," + Base64.encode(qrcodeBytes));
                return qrCodeVO;
            }
        } catch (IOException | WriterException e) {
            throw new RuntimeException("获取B站二维码异常", e);
        }
        throw new RuntimeException("获取B站二维码异常");
    }

    @GetMapping("login")
    public BiliLoginVO login(@RequestParam String qrCodeKey) {
        try (HttpResponse response = HttpRequest.get(BilibiliAPI.GET_QR_CODE_LOGIN_INFO_URL).body("qrcode_key=" + qrCodeKey).execute()) {
        /*
        当密钥正确时但未扫描时code为86101
        扫描成功但手机端未确认时code为86090
        扫描成功手机端确认登录后，code为0，并向浏览器写入cookie
        二维码失效时code为86038
         */
            JSONObject jsonBody = JSONUtil.parseObj(response.body());
            JSONObject data = jsonBody.getJSONObject("data");
            int code = data.getInt("code");

            BiliLoginVO biliLoginVO = new BiliLoginVO();
            if (code == 0) {
                String decodeUrl = URLUtil.decode(data.getStr("url"));
                String sessdata;
                try {
                    Map<String, Object> urlParams = getUrlParams(new URL(decodeUrl).getQuery());
                    sessdata = ((String) urlParams.get("SESSDATA"));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                String biliJct = response.getCookieValue("bili_jct");
                String dedeuserid = response.getCookieValue("DedeUserID");

                // 存在用户时更新cookie
                userService.save(dedeuserid, sessdata, biliJct);
                taskConfigService.updateCookie(dedeuserid, sessdata, biliJct);

                biliLoginVO.setBiliJct(biliJct);
                biliLoginVO.setDedeuserid(Integer.parseInt(dedeuserid));
                biliLoginVO.setSessdata(sessdata);
                biliLoginVO.setCode(0);

                return biliLoginVO;
            }

            biliLoginVO.setCode(code);
            return biliLoginVO;
        }
    }
}
