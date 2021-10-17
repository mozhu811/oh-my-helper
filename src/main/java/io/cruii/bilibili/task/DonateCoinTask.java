package io.cruii.bilibili.task;

import cn.hutool.json.JSONObject;
import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.context.BilibiliUserContext;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 投币任务
 *
 * @author cruii
 * Created on 2021/9/16
 */
@Log4j2
public class DonateCoinTask extends VideoTask {
    private final TaskConfig config;
    private int counter = 0;

    public DonateCoinTask(BilibiliDelegate delegate) {
        super(delegate);
        this.config = delegate.getConfig();
    }

    @Override
    public void run() {
        checkAttemptsAndChangeProxy();
        addAttempts();

        // 防止全部都投过币而导致任务卡死
        counter++;
        if (counter > 3) {
            initFollowList();
            initTrend();
            counter = 0;
        }

        BilibiliUser user = BilibiliUserContext.get();
        if (user.getLevel() >= 6) {
            log.info("账号已到达6级，取消执行投币任务");
            return;
        }

        Integer donateCoins = config.getDonateCoins();
        log.info("配置投币数为：{}", donateCoins);
        int actual = calDiff();
        if (actual <= 0) {
            log.info("今日投币任务已完成 ✔️");
            return;
        } else {
            log.info("距完成任务还需投币{}个", actual);
        }
        // 获取账户余额
        int current = getCoin();
        log.info("当前账户余额：{}", current);
        if (current <= config.getReserveCoins() ||
                actual > current) {
            log.info("当前余额不足或触发硬币保护阈值，取消执行投币任务。❌");
            return;
        }

        // 获取投币策略
        Integer donatePriority = config.getDonateCoinStrategy();
        List<String> bvidList;
        if (donatePriority == 1) {
            // 热榜投币
            Collections.shuffle(trend);
            bvidList = trend.stream().limit(actual).collect(Collectors.toList());
        } else {
            // 动态列表投币
            Collections.shuffle(follow);
            bvidList = follow.stream().limit(actual).collect(Collectors.toList());
        }

        boolean done = doDonate(bvidList);

        // 若为完成当日任务重复执行
        if (!done) {
            run();
        } else {
            log.info("今日投币任务已完成 ✔️");
        }
    }

    @Override
    public String getName() {
        return "投币任务";
    }

    /**
     * 计算还需多少硬币完成任务
     *
     * @return 所需硬币数
     */
    private int calDiff() {
        JSONObject coinExpToday = delegate.getCoinExpToday();
        Integer data = coinExpToday.getInt("data");
        return config.getDonateCoins() - data / 10;
    }

    /**
     * 获取账号当前硬币余额
     *
     * @return 硬币余额
     */
    private int getCoin() {
        JSONObject resp = delegate.getCoin();
        if (resp.getInt(CODE) == 0) {
            BigDecimal coin = resp.getByPath("data.money", BigDecimal.class);
            if (coin == null) {
                return 0;
            }
            return coin.intValue();
        }
        return 0;
    }

    /**
     * 执行投币
     *
     * @param bvidList 投币视频列表
     * @return 是否完成投币任务
     */
    private boolean doDonate(List<String> bvidList) {
        bvidList.stream()
                .filter(bvid -> {
                    JSONObject resp = delegate.checkDonateCoin(bvid);
                    if (resp.getByPath("data.multiply", Integer.class) > 0) {
                        String videoTitle = getVideoTitle(bvid);
                        log.info("已为视频[{}]投过币，本次跳过", videoTitle);
                        bvidList.remove(bvid);
                        return false;
                    }
                    return true;
                })
                .forEach(bvid -> {
                    JSONObject resp = delegate.donateCoin(bvid, 1, 1);
                    String videoTitle = getVideoTitle(bvid);
                    if (resp.getInt(CODE) == 0) {
                        log.info("为视频[{}]投币成功 ✔️", videoTitle);
                    } else {
                        log.error("为视频[{}]投币失败 ❌", resp.getStr(MESSAGE));
                    }
                });

        return calDiff() <= 0;
    }
}
