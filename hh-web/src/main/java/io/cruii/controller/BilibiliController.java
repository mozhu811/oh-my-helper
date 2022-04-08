package io.cruii.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.zxing.WriterException;
import io.cruii.constant.BilibiliAPI;
import io.cruii.exception.BilibiliCookieExpiredException;
import io.cruii.exception.BilibiliUserNotFoundException;
import io.cruii.pojo.vo.BilibiliLoginVO;
import io.cruii.pojo.vo.BilibiliUserVO;
import io.cruii.pojo.vo.QrCodeVO;
import io.cruii.service.BilibiliUserService;
import io.cruii.service.TaskService;
import io.cruii.util.QrCodeGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private final BilibiliUserService userService;
    private final TaskService taskService;

    public BilibiliController(BilibiliUserService userService,
                              TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @GetMapping("user")
    public BilibiliUserVO getBilibiliUser(@RequestParam String dedeuserid,
                                          @RequestParam String sessdata) {
        HttpCookie sessdataCookie = new HttpCookie("SESSDATA", sessdata);
        sessdataCookie.setDomain(".bilibili.com");
        String body = HttpRequest.get(BilibiliAPI.GET_USER_INFO_NAV)
                .cookie(sessdataCookie)
                .execute().body();
        JSONObject resp = JSONUtil.parseObj(body);
        if (resp.getInt("code") == -101) {
            throw new BilibiliCookieExpiredException(dedeuserid);
        }

        JSONObject data = resp.getJSONObject("data");
        String mid = data.getStr("mid");

        // 找不到用户
        if (mid == null || !mid.equals(dedeuserid)) {
            throw new BilibiliUserNotFoundException(dedeuserid);
        }

        String face = data.getStr("face");

        InputStream inputStream = HttpRequest
                .get(face)
                .execute().bodyStream();

        return new BilibiliUserVO()
                .setAvatar("data:image/jpeg;base64," + Base64.encode(inputStream))
                .setUsername(data.getStr("uname"))
                .setConfigId(taskService.getTask(dedeuserid) == null ? null : taskService.getTask(dedeuserid).getId())
                .setLevel(data.getJSONObject("level_info")
                        .getInt("current_level"));
    }

    @GetMapping("users")
    public Page<BilibiliUserVO> listUsers(@RequestParam Integer page,
                                          @RequestParam Integer size) {
        if (page <= 0 ){
            page = 1;
        }

        if (size < 30) {
            size = 30;
        }
        return userService.list(page, size);
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
            String sessdata = response.getCookieValue("SESSDATA");

            /*
            如果在新用户没有创建任务时就保存信息，则造成前端会显示未创建任务的用户信息
            这样不符合逻辑。
            所以，只有在老用户登录时才更新cookie，新用户直接返回即可。
             */
            if (taskService.isExist(dedeuserid)) {
                // 存在用户时更新cookie
                userService.save(dedeuserid, sessdata, biliJct);
                taskService.updateCookie(dedeuserid, sessdata, biliJct);
            }

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
