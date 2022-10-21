package io.cruii.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.zxing.WriterException;
import io.cruii.constant.BilibiliAPI;
import io.cruii.exception.BilibiliCookieExpiredException;
import io.cruii.exception.BilibiliUserNotFoundException;
import io.cruii.pojo.dto.BilibiliUserDTO;
import io.cruii.pojo.vo.BilibiliLoginVO;
import io.cruii.pojo.vo.BilibiliUserVO;
import io.cruii.pojo.vo.QrCodeVO;
import io.cruii.service.BilibiliUserService;
import io.cruii.service.TaskConfigService;
import io.cruii.util.QrCodeGenerator;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
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

    private final MapperFactory mapperFactory;
    public BilibiliController(BilibiliUserService userService,
                              TaskConfigService taskConfigService, MapperFactory mapperFactory) {
        this.userService = userService;
        this.taskConfigService = taskConfigService;
        this.mapperFactory = mapperFactory;
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
    public BilibiliUserVO getBilibiliUser(@RequestParam String dedeuserid, @RequestParam String sessdata) {
        HttpCookie sessdataCookie = new HttpCookie("SESSDATA", sessdata);
        sessdataCookie.setDomain(".bilibili.com");
        JSONObject data;
        try(HttpResponse response = HttpRequest.get(BilibiliAPI.GET_USER_INFO_NAV).cookie(sessdataCookie).execute()) {
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

        String face = data.getStr("face");
        try (HttpResponse response = HttpRequest.get(face).execute()) {
            InputStream inputStream = response.bodyStream();
            return new BilibiliUserVO().setAvatar("data:image/jpeg;base64," + Base64.encode(inputStream)).setUsername(data.getStr("uname"))
                    .setConfigId(taskConfigService.getTask(dedeuserid) == null ? null : taskConfigService.getTask(dedeuserid).getId()).setLevel(data.getJSONObject("level_info").getInt("current_level"));
        }

    }

    @PutMapping("user")
    @ResponseStatus(HttpStatus.CREATED)
    public void updateUser(@RequestBody BilibiliUserVO bilibiliUserVO) {
        userService.save(mapperFactory.getMapperFacade().map(bilibiliUserVO, BilibiliUserDTO.class));
    }

    @GetMapping("users")
    public Page<BilibiliUserVO> listUsers(@RequestParam Integer page, @RequestParam Integer size) {
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
        try (HttpResponse response = HttpRequest.get(BilibiliAPI.GET_QR_CODE_LOGIN_URL).execute()) {
            log.debug(response);
            if (response.getStatus() == 200) {
                JSONObject jsonBody = JSONUtil.parseObj(response.body());
                if (jsonBody.getInt("code") == 0) {
                    JSONObject data = jsonBody.getJSONObject("data");
                    String qrCodeUrl = data.getStr("url");
                    String qrCodeKey = data.getStr("qrcode_key");

                    QrCodeVO qrCodeVO = new QrCodeVO();
                    qrCodeVO.setQrCodeUrl(qrCodeUrl);
                    qrCodeVO.setQrCodeKey(qrCodeKey);

                    byte[] bytes = QrCodeGenerator.generateQrCode(qrCodeUrl, 180, true);
                    qrCodeVO.setQrCodeImg("data:image/png;base64," + Base64.encode(bytes));
                    return qrCodeVO;
                }
            }
        } catch (WriterException | IOException e) {
            throw new RuntimeException("获取B站二维码登录链接异常", e);
        }
        throw new RuntimeException("获取B站二维码登录链接异常");
    }

    @GetMapping("login")
    public BilibiliLoginVO login(@RequestParam String qrCodeKey) {
        try (HttpResponse response = HttpRequest.get(BilibiliAPI.GET_QR_CODE_LOGIN_INFO_URL).body("qrcode_key=" + qrCodeKey).execute()) {
        /*
        当密钥正确时但未扫描时code为86101
        扫描成功但手机端未确认时code为86090
        扫描成功手机端确认登录后，code为0，并向浏览器写入cookie
        二维码失效时code为86038
         */
            log.debug(response);
            JSONObject jsonBody = JSONUtil.parseObj(response.body());
            /*
                {"code":0,"message":"0","ttl":1,
                "data":{"url":"https://passport.biligame.com/crossDomain?DedeUserID=287969457\u0026DedeUserID__ckMd5=e6f2d9a03ca037bd\u0026Expires=1681227537\u0026SESSDATA=605d4f97,1681227537,eac30*a1\u0026bili_jct=94896bdce596eff2d226d531d2b0bd85\u0026gourl=https%3A%2F%2Fwww.bilibili.com",
                "refresh_token":"f92c6c0217805359b0c40d614462c8a1","timestamp":1665675537415,"code":0,"message":""}}
             */
            JSONObject data = jsonBody.getJSONObject("data");
            int code = data.getInt("code");

            BilibiliLoginVO bilibiliLoginVO = new BilibiliLoginVO();
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
                log.info("{}-{}-{}", biliJct, dedeuserid, sessdata);
            /*
            如果在新用户没有创建任务时就保存信息，则造成前端会显示未创建任务的用户信息
            这样不符合逻辑。
            所以，只有在老用户登录时才更新cookie，新用户直接返回即可。
             */
                if (taskConfigService.isExist(dedeuserid)) {
                    // 存在用户时更新cookie
                    userService.save(dedeuserid, sessdata, biliJct);
                    taskConfigService.updateCookie(dedeuserid, sessdata, biliJct);
                }

                bilibiliLoginVO.setBiliJct(biliJct);
                bilibiliLoginVO.setDedeuserid(Integer.parseInt(dedeuserid));
                bilibiliLoginVO.setSessdata(sessdata);
                bilibiliLoginVO.setCode(0);

                return bilibiliLoginVO;
            }

            bilibiliLoginVO.setCode(code);
            return bilibiliLoginVO;
        }
    }
}
