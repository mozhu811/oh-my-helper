package io.cruii.task;

import cn.hutool.json.JSONObject;
import io.cruii.component.BilibiliDelegate;
import lombok.extern.log4j.Log4j2;

/**
 * 漫画签到任务
 *
 * @author cruii
 * Created on 2021/9/16
 */
@Log4j2
public class MangaTask extends AbstractTask {
    public MangaTask(BilibiliDelegate delegate) {
        super(delegate);
    }

    @Override
    public void run() {
        JSONObject resp = delegate.mangaCheckIn(delegate.getConfig().getDevicePlatform());
        if (resp != null && "0".equals(resp.getStr(CODE).trim())) {
            log.info("漫画签到成功 ✔️");
        } else if (resp != null && MANGA_CLOCK_IN_DUPLICATE.equals(resp.getStr("msg"))) {
            log.info("已完成漫画签到 ✔️ ");
        } else {
            log.error("漫画签到失败：{} ❌", resp);
        }
    }

    @Override
    public String getName() {
        return "漫画签到";
    }
}
