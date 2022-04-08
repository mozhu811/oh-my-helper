package io.cruii.task;

import cn.hutool.json.JSONObject;
import io.cruii.component.BilibiliDelegate;
import lombok.extern.log4j.Log4j2;

/**
 * @author cruii
 * Created on 2021/9/22
 */
@Log4j2
public class ReadMangaTask extends AbstractTask {
    public ReadMangaTask(BilibiliDelegate delegate) {
        super(delegate);
    }

    @Override
    public void run() {
        checkAttemptsAndChangeProxy();
        addAttempts();

        JSONObject resp = delegate.readManga();
        if (resp.getInt(CODE) == 0) {
            log.info("完成漫画阅读 ✔️");
        } else {
            log.info("阅读失败：{} ❌", resp);
        }
    }

    @Override
    public String getName() {
        return "阅读漫画";
    }
}
