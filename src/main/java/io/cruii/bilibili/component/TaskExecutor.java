package io.cruii.bilibili.component;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpException;
import cn.hutool.json.JSONObject;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.task.*;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author cruii
 * Created on 2021/9/15
 */
@Log4j2
public class TaskExecutor {

    private final List<Task> taskList = new ArrayList<>();
    private final BilibiliDelegate delegate;

    public TaskExecutor(BilibiliDelegate delegate) {
        this.delegate = delegate;
        taskList.add(new WatchVideoTask(delegate));
        taskList.add(new MangaTask(delegate));
        taskList.add(new DonateCoinTask(delegate));
        taskList.add(new Silver2CoinTask(delegate));
        taskList.add(new LiveCheckIn(delegate));
        taskList.add(new DonateGiftTask(delegate));
        taskList.add(new ChargeTask(delegate));
        taskList.add(new GetVipPrivilegeTask(delegate));
        taskList.add(new ReadMangaTask(delegate));
    }

    public void execute() {
        Collections.shuffle(taskList);
        taskList.add(new GetCoinChangeLogTask(delegate));
        taskList.add(new CheckCookieTask(delegate));
        Collections.reverse(taskList);
        taskList.forEach(task -> {
            try {
                log.info("[{}]", task.getName());
                task.run();
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (HttpException | IORuntimeException e) {
                log.debug("HTTP访问异常: {}, 切换代理地址", e.getMessage());
            }
        });
        log.info("[所有任务已执行完成]");

        calExp();

        afterFinish();
    }

    private void afterFinish() {
        try {
            TaskRunner.FINISH_QUEUE.put(MDC.get("traceId"));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void calExp() {
        JSONObject coinExpToday = delegate.getCoinExpToday();
        int exp = coinExpToday.getInt("data") + 15;
        log.info("今日已获得[{}]点经验",  exp);
        BilibiliUser user = delegate.getUser();
        if (user.getLevel() < 6) {
            int upgradeDays = (user.getNextExp() - user.getCurrentExp()) / exp;
            log.info("按照当前进度，升级到Lv{}还需要: {}天", user.getLevel() + 1, upgradeDays + 1);
        } else {
            log.info("当前等级Lv6，经验值为：{}", user.getCurrentExp());
        }
    }
}
