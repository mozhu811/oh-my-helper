package io.cruii.bilibili.component;

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
    private final TaskConfig config;

    public TaskExecutor(TaskConfig config) {
        this.config = config;
        taskList.add(new WatchVideoTask(config));
        taskList.add(new MangaTask(config));
        taskList.add(new DonateCoinTask(config));
        taskList.add(new Silver2CoinTask(config));
        taskList.add(new LiveCheckIn(config));
        taskList.add(new DonateGiftTask(config));
        taskList.add(new ChargeTask(config));
        taskList.add(new GetVipPrivilegeTask(config));
        taskList.add(new ReadMangaTask(config));
    }

    public void execute() {
        Collections.shuffle(taskList);
        taskList.add(new CheckCookieTask(config));
        Collections.reverse(taskList);
        taskList.forEach(task -> {
            log.info("[{}]", task.getName());
            task.run();
            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });
        log.info("[所有任务已执行完成]");
        calExp();
        try {
            TaskRunner.FINISH_QUEUE.put(MDC.get("traceId"));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void calExp() {
        BilibiliDelegate delegate = new BilibiliDelegate(config);
        JSONObject coinExpToday = delegate.getCoinExpToday();
        int exp = coinExpToday.getInt("data") + 15;
        log.info("今日已获得[{}]点经验",  exp);
        BilibiliUser user = delegate.getUser();
        if (user.getLevel() < 6) {
            int upgradeDays = (user.getNextExp() - user.getCurrentExp()) / exp;
            log.info("按照当前进度，升级到Lv{}还需要: {}天", user.getLevel() + 1, upgradeDays);
        } else {
            log.info("当前等级Lv6，经验值为：{}", user.getCurrentExp());
        }
    }
}
