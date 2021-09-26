package io.cruii.bilibili.component;

import cn.hutool.json.JSONObject;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.task.*;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cruii
 * Created on 2021/9/15
 */
@Log4j2
public class BilibiliTaskExecutor {

    private final List<Task> taskList = new ArrayList<>();
    private final TaskConfig config;

    public BilibiliTaskExecutor(TaskConfig config) {
        this.config = config;
        taskList.add(new CheckCookieTask(config));
        taskList.add(new GetCoinChangeLogTask(config));
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
        taskList.forEach(task -> {
            log.info("----执行[{}]开始----", task.getName());
            task.run();
            log.info("----执行[{}]结束----", task.getName());
        });
        log.info("----[所有任务已执行完成]----");
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
        Integer exp = coinExpToday.getInt("data");
        log.info("今日已获得[{}]点经验", 15 + exp);
        BilibiliUser user = delegate.getUser();
        if (user.getLevel() < 6) {
            log.info("按照当前进度，升级到Lv{}还需要: {}天", user.getLevel() + 1,
                    (user.getNextExp() - user.getCurrentExp()) / exp);
        } else {
            log.info("当前等级Lv6，经验值为：{}", user.getCurrentExp());
        }
    }
}
