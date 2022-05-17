package io.cruii.component;

import cn.hutool.json.JSONObject;
import io.cruii.context.BilibiliUserContext;
import io.cruii.exception.BilibiliCookieExpiredException;
import io.cruii.pojo.po.BilibiliUser;
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
    private final BilibiliDelegate delegate;

    public TaskExecutor(BilibiliDelegate delegate) {
        this.delegate = delegate;
        taskList.add(new CheckCookieTask(delegate));
        taskList.add(new GetCoinChangeLogTask(delegate));
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

    public BilibiliUser execute() throws Exception {
        boolean expired = false;
        for (Task task : taskList) {
            try {
                log.info("[{}]", task.getName());
                task.run();
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BilibiliCookieExpiredException e) {
                expired = true;
                break;
            } catch (Exception e) {
                log.error("[{}]任务执行失败",  task.getName(), e);
            }
        }

        BilibiliUser user = BilibiliUserContext.get();

        if (!expired) {
            log.info("[所有任务已执行完成]");
            try {
                user = calExp();
            } catch (Exception e) {
                log.error("计算经验失败", e);
                user = BilibiliUserContext.get();
            }
        }
        return user;
    }

    private BilibiliUser calExp() {
        BilibiliUser user = BilibiliUserContext.get();
        int current = delegate.getExp();
        user.setCurrentExp(current);
        int exp = 0;
        // 获取当日获取的经验
        JSONObject expRewardStatus = delegate.getExpRewardStatus();
        JSONObject body = expRewardStatus.getJSONObject("data");
        Boolean share = body.getBool("share", false);
        Boolean watch = body.getBool("watch", false);
        Boolean login = body.getBool("login", false);
        Integer coinExp = body.getInt("coins", 0);

        if (Boolean.TRUE.equals(share)) {
            exp += 5;
        }

        if (Boolean.TRUE.equals(watch)) {
            exp += 5;
        }

        if (Boolean.TRUE.equals(login)) {
            exp += 5;
        }

        exp += coinExp;
        log.info("今日已获得[{}]点经验", exp);
        if (exp > 0) {
            if (user.getLevel() < 6) {
                int diff = user.getNextExp() - user.getCurrentExp();

                int days = (diff / exp) + 1;
                user.setUpgradeDays(days);
                if (diff <= exp) {
                    user.setUpgradeDays(null);
                }
            } else {
                user.setUpgradeDays(null);
            }

            if (user.getLevel() < 6) {
                int upgradeDays = (user.getNextExp() - user.getCurrentExp()) / exp;
                log.info("按照当前进度，升级到Lv{}还需要: {}天", user.getLevel() + 1, upgradeDays + 1);
            } else {
                log.info("当前等级Lv6，经验值为：{}", user.getCurrentExp());
            }
        } else {
            log.error("经验值为0，无法计算升级天数");
        }

        return user;
    }
}
