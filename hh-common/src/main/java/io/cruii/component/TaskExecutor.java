package io.cruii.component;

import cn.hutool.json.JSONObject;
import io.cruii.context.BilibiliUserContext;
import io.cruii.exception.BilibiliCookieExpiredException;
import io.cruii.model.BiliUser;
import io.cruii.task.*;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author cruii
 * Created on 2021/9/15
 */
@Log4j2
public class TaskExecutor {
    private final List<Task> taskList = new ArrayList<>();

    public TaskExecutor(BilibiliDelegate delegate) {
        taskList.add(new CheckCookieTask(delegate));
        taskList.add(new GetCoinChangeLogTask(delegate));
        taskList.add(new WatchVideoTask(delegate));
        taskList.add(new DonateCoinTask(delegate));
        taskList.add(new Silver2CoinTask(delegate));
        taskList.add(new LiveCheckIn(delegate));
        taskList.add(new DonateGiftTask(delegate));
        taskList.add(new ChargeTask(delegate));
        taskList.add(new GetVipPrivilegeTask(delegate));
        taskList.add(new MangaTask(delegate));
        taskList.add(new ReadMangaTask(delegate));
        taskList.add(new BigVipTask(delegate));
        //taskList.add(new CalExpTask(delegate));
    }

    public BiliUser execute() throws Exception {
        log.info("------开始------");
        for (Task task : taskList) {
            try {
                log.info("[{}]", task.getName());
                TimeUnit.SECONDS.sleep(2);
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BilibiliCookieExpiredException e) {
                break;
            } catch (Exception e) {
                log.error("[{}]任务执行失败: {}", task.getName(), e.getCause(), e);
            }
        }
        log.info("------结束------");

        // todo 计算经验值
        return BilibiliUserContext.get();
    }
}
