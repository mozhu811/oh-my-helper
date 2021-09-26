package io.cruii.bilibili.task;

import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cruii
 * Created on 2021/9/15
 */
@Log4j2
public class BilibiliTaskExecutorTest {
    private final List<Task> taskList = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        TaskConfig config = new TaskConfig();
        config.setDedeuserid("287969457");
        config.setSessdata("7f092113%2C1647394552%2C8b09b%2A91");
        config.setBiliJct("dc9e5a2826e63769f5c3c78c31f15b81");
        config.setTaskIntervalTime(10);
        config.setNumberOfCoins(5);
        config.setReserveCoins(50);
        config.setSelectLike(1);
        config.setMonthEndAutoCharge(true);
        config.setGiveGift(true);
        config.setUpLive("9253035");
        config.setChargeForLove("0");
        config.setDevicePlatform("ios");
        config.setCoinAddPriority(1);
        config.setUserAgent("user-agent");
        config.setTelegrambottoken("tgtoken");
        config.setTelegramchatid("tgchatid");
        config.setServerpushkey("serverpushkey");
        config.setEmail("cruii811@gmail.com");
        config.setSkipDailyTask(false);

        taskList.add(new CheckCookieTask(config));
//        taskList.add(new GetCoinChangeLogTask(config));
//        taskList.add(new WatchVideoTask(config));
//        taskList.add(new MangaTask(config));
//        taskList.add(new DonateCoinTask(config));
//        taskList.add(new Silver2CoinTask(config));
//        taskList.add(new LiveCheckIn(config));
        taskList.add(new DonateGiftTask(config));
    }

    @Test
    public void execute() {
        log.info("======开始执行任务======");
        taskList.forEach(task -> {
            log.info("======执行[{}]开始======", task.getName());
            task.run();
            log.info("======执行[{}]结束======", task.getName());
        });
    }
}