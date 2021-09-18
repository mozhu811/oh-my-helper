package io.cruii.bilibili.task;

import cn.hutool.json.JSONObject;
import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;

/**
 * @author cruii
 * Created on 2021/9/17
 */
@Log4j2
public class LiveCheckIn extends AbstractTask {
    public LiveCheckIn(TaskConfig config) {
        super(config);
    }

    @Override
    public void run() {
        JSONObject resp = delegate.liveCheckIn();
        if (resp.getInt(CODE) == 0) {
            log.info("直播签到成功，本次获得{},{}", resp.getByPath("data.text", String.class),
                    resp.getByPath("data.specialText", String.class));
        } else {
            log.debug("直播签到失败: {}", resp.getStr(MESSAGE));
        }
    }

    @Override
    public String getName() {
        return "直播签到";
    }
}
