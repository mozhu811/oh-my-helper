package io.cruii.bilibili.push;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.bilibili.config.BiliPusherConfig;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.mapper.TaskConfigMapper;

import java.net.HttpCookie;
import java.util.HashMap;

/**
 * @author cruii
 * Created on 2021/11/24
 */
public class BiliPusher implements Pusher {
    private final String targetUserId;
    private final String sendUserId;
    private final String sessdata;
    private final String biliJct;

    public BiliPusher(String targetUserId) {
        BiliPusherConfig biliPusherConfig = SpringUtil.getApplicationContext().getBean(BiliPusherConfig.class);
        this.targetUserId = targetUserId;
        sendUserId = biliPusherConfig.getDedeuserid();

        TaskConfigMapper taskConfigMapper = SpringUtil.getApplicationContext().getBean(TaskConfigMapper.class);
        TaskConfig config = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class).eq(TaskConfig::getDedeuserid, sendUserId));
        sessdata = config.getSessdata();
        biliJct = config.getBiliJct();
    }

    /**
     * 该方法通过B站私信推送消息
     * 但该消息只能在 web 端看到，所以暂时不启用
     *
     * @param content 推送内容
     * @return 是否发送成功
     */
    @Override
    public boolean push(String content) {
        HashMap<String, String> params = new HashMap<>();
        params.put("msg[sender_uid]", sendUserId);
        params.put("msg[receiver_id]", targetUserId);
        params.put("msg[receiver_type]", "1");
        params.put("msg[msg_type]", "1");
        String deviceId = RandomUtil.randomStringUpper(8)
                + "-"
                + RandomUtil.randomStringUpper(4)
                + "-"
                + RandomUtil.randomStringUpper(4)
                + "-"
                + RandomUtil.randomStringUpper(4)
                + "-"
                + RandomUtil.randomStringUpper(12);
        params.put("msg[dev_id]", deviceId);
        params.put("msg[timestamp]", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("msg[content]", content);
        params.put("csrf", biliJct);
        HttpRequest request = HttpRequest.post("http://api.vc.bilibili.com/web_im/v1/web_im/send_msg")
                .body(HttpUtil.toParams(params))
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .cookie(new HttpCookie("DedeUserID", sendUserId), new HttpCookie("SESSDATA", sessdata), new HttpCookie("bili_jct", biliJct));
        String responseBody = request.execute().body();
        System.out.println(responseBody);
        return JSONUtil.parseObj(responseBody).getInt("code") == 0;
    }
}
