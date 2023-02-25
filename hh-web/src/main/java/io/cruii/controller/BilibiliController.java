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
import io.cruii.model.BiliQrcode;
import io.cruii.model.BiliUser;
import io.cruii.pojo.vo.*;
import io.cruii.service.BilibiliUserService;
import io.cruii.service.TaskConfigService;
import io.cruii.util.CosUtil;
import io.cruii.util.OkHttpUtil;
import io.cruii.util.QrCodeGenerator;
import lombok.extern.log4j.Log4j2;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.HttpURLConnection;
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
        BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, null, false);
        BiliUser userDetails = delegate.getUserDetails();
        new Thread(() -> {
            String face = userDetails.getFace();
            byte[] bytes;
            try {
                bytes = imageToArray(face);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            File file = saveAvatar(bytes, dedeuserid);
            CosUtil.upload(file);
        }).start();

        TaskConfigVO taskConfigVO = taskConfigService.getTask(dedeuserid);
        Long configId = null;
        if (taskConfigVO != null) {
            configId = taskConfigVO.getId();
        }
        return new OmhUserVO()
                .setUserId(dedeuserid)
                .setNickname(userDetails.getName())
                .setBiliTaskConfigId(configId);
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

        try (Response qrCodeResponse = OkHttpUtil.executeWithRetry(request)) {
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
                BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct);
                String avatar = delegate.getUserDetails().getFace();
                if (avatar != null) {
                    new Thread(() -> {
                        try {
                            byte[] bytes = imageToArray(avatar);
                            File file = saveAvatar(bytes, dedeuserid);
                            CosUtil.upload(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
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

    private byte[] imageToArray(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        InputStream inStream = conn.getInputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] imageBytes = outStream.toByteArray();
        outStream.close();
        inStream.close();
        conn.disconnect();
        return imageBytes;
    }

    private File saveAvatar(byte[] bytes, String dedeuserid) {
        String path = "avatars" + File.separator + dedeuserid + ".png";

        File file = new File(path);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(bytes);
            outputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
