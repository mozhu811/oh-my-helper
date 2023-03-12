package io.cruii.execution.component;

import cn.hutool.core.io.resource.ResourceUtil;
import io.cruii.component.BilibiliDelegate;
import io.cruii.context.BilibiliUserContext;
import io.cruii.exception.BilibiliCookieExpiredException;
import io.cruii.execution.constant.TaskStatus;
import io.cruii.model.BiliDailyReward;
import io.cruii.model.BiliUser;
import io.cruii.model.custom.BiliTaskResult;
import io.cruii.task.*;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author cruii
 * Created on 2021/9/15
 */
@Log4j2
public class TaskExecutor {
    private final List<Task> taskList = new ArrayList<>();

    private final BilibiliDelegate delegate;

    private static final Properties PROP;

    static {
        try {
            PROP = new Properties();
            PROP.load(ResourceUtil.getStream("version.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TaskExecutor(BilibiliDelegate delegate) {
        this.delegate = delegate;
        taskList.add(new GetCoinChangeLogTask(delegate));
        taskList.add(new WatchVideoTask(delegate));
        taskList.add(new GetVipPrivilegeTask(delegate));
        taskList.add(new DonateCoinTask(delegate));
        taskList.add(new Silver2CoinTask(delegate));
        taskList.add(new LiveCheckIn(delegate));
        taskList.add(new DonateGiftTask(delegate));
        taskList.add(new ChargeTask(delegate));
        taskList.add(new MangaTask(delegate));
        taskList.add(new ReadMangaTask(delegate));
        taskList.add(new BigVipTask(delegate));
    }

    public BiliTaskResult execute() {
        log.info("当前版本: {}", PROP.getProperty("version"));
        log.info("更新日期: {}", PROP.getProperty("release.date"));
        log.info("服务地址: {}", "https://ohmyhelper.com/bilibili");
        log.info("项目源码: {}", "https://github.com/Cruii/oh-my-helper");
        log.info("------开始------");
        boolean expired = false;
        for (Task task : taskList) {
            try {
                log.info("[{}]", task.getName());
                TimeUnit.SECONDS.sleep(2);
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[{}]任务被中断: {}", task.getName(), e.getMessage(), e);
            } catch (BilibiliCookieExpiredException e) {
                expired = true;
                break;
            } catch (Exception e) {
                log.error("[{}]任务执行失败: {}", task.getName(), e.getMessage(), e);
            }
        }
        log.info("------结束------");
        int upgradeDays = 0;
        if (!expired) {
            upgradeDays = calExp();
        }
        BiliUser biliUser = BilibiliUserContext.get();
        return new BiliTaskResult(TaskStatus.SUCCESS, biliUser, upgradeDays);
    }

    private int calExp() {
        // 默认登录5点经验
        int expToday = 5;
        BiliUser biliUser = delegate.getUserDetails();
        expToday += delegate.getCoinExpToday();
        BiliDailyReward rewardStatus = delegate.getExpRewardStatus();

        if (Boolean.TRUE.equals(rewardStatus.getShare())) {
            expToday += 5;
        }
        if (Boolean.TRUE.equals(rewardStatus.getWatch())) {
            expToday += 5;
        }

        log.info("今日已获得[{}]点经验", expToday);

        BiliUser.LevelExp levelExp = biliUser.getLevelExp();
        if (levelExp.getCurrentLevel() < 6) {
            int diff = levelExp.getNextExp() - levelExp.getCurrentExp();
            int days = (diff / expToday) + 1;
            log.info("按照当前进度，升级到Lv{}还需要: {}天", levelExp.getCurrentLevel() + 1, days);
            return days;
        } else {
            log.info("当前等级Lv6，经验值为：{}", levelExp.getCurrentExp());
        }
        return -1;
    }
}
