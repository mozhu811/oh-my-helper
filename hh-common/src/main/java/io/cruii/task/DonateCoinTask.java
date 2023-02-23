package io.cruii.task;

import cn.hutool.json.JSONObject;
import io.cruii.component.BilibiliDelegate;
import io.cruii.context.BilibiliUserContext;
import io.cruii.pojo.entity.TaskConfigDO;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 投币任务
 *
 * @author cruii
 * Created on 2021/9/16
 */
@Log4j2
public class DonateCoinTask extends VideoTask {
    private final TaskConfigDO config;
    private int counter = 0;

    private int coinNum;

    public DonateCoinTask(BilibiliDelegate delegate) {
        super(delegate);
        this.config = delegate.getConfig();
        Integer donateCoins = config.getDonateCoins();
        log.info("配置投币数为：{}", donateCoins);
        coinNum = calDiff();
    }

    @Override
    public void run() {
        // 防止全部都投过币而导致任务卡死
        counter++;
        if (counter > 3) {
            initTrend();
            counter = 0;
        }

        if (BilibiliUserContext.get().getLevel() >= 6) {
            log.info("账号已到达6级，取消执行投币任务");
            return;
        }

        // 获取账户余额
        int current = getCoin();
        log.info("当前账户余额：{}", current);
        if (current <= config.getReserveCoins() ||
                coinNum > current) {
            log.info("当前余额不足或触发硬币保护阈值，取消执行投币任务。❌");
            return;
        }

        int coinExpToday = delegate.getCoinExpToday();
        if (coinExpToday / 10 - config.getDonateCoins() >= 0) {
            log.info("今日投币任务已完成，取消执行投币任务");
            return;
        }

        doDonate();
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
        int coinExpToday = delegate.getCoinExpToday();
        return config.getDonateCoins() - coinExpToday / 10;
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
     */
    private void doDonate() {
        // 获取服务器上的投币信息，防止在任务执行期间用户也投了币导致的多投。
        while (calDiff() > 0) {
            initTrend();
            List<String> bvidList;
            // 热榜投币
            Collections.shuffle(trend);
            bvidList = trend.stream().limit(coinNum).collect(Collectors.toList());
            bvidList.stream()
                    .filter(bvid -> {
                        // 若视频已投币，则跳过
                        JSONObject resp = delegate.checkDonateCoin(bvid);
                        return resp.getByPath("data.multiply", Integer.class) < 1;
                    })
                    .forEach(bvid -> {
                        JSONObject resp = delegate.donateCoin(bvid, 1, 1);
                        String videoTitle = getVideoTitle(bvid);
                        if (resp.getInt(CODE) == 0) {
                            log.info("为视频[{}]投币成功 ✔️", videoTitle);
                            --coinNum;
                        } else {
                            log.warn("为视频[{}]投币失败: {} ❌", videoTitle, resp.getStr(MESSAGE));
                        }
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
        }
        log.info("今日投币任务已完成 ✔️");
    }
}
