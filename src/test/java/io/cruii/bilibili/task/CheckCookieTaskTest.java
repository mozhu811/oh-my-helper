package io.cruii.bilibili.task;

import io.cruii.bilibili.entity.TaskConfig;
import org.junit.Test;

/**
 * @author cruii
 * Created on 2021/9/15
 */
public class CheckCookieTaskTest {

    @Test
    public void run() {
        TaskConfig config = new TaskConfig();
        config.setDedeuserid("287969457");
        config.setSessdata("6d8c75a8%2C1646443725%2Ca2892%2A91");
        config.setBiliJct("85cce5987f4036d1d1d66af3e94f9504");
        config.setTaskIntervalTime(10);
        config.setNumberOfCoins(5);
        config.setReserveCoins(50);
        config.setSelectLike(1);
        config.setMonthEndAutoCharge(true);
        config.setGiveGift(true);
        config.setUpLive("287969457");
        config.setChargeForLove("0");
        config.setDevicePlatform("ios");
        config.setCoinAddPriority(1);
        config.setUserAgent("user-agent");
        config.setTelegrambottoken("tgtoken");
        config.setTelegramchatid("tgchatid");
        config.setServerpushkey("serverpushkey");
        config.setEmail("cruii811@gmail.com");
        config.setSkipDailyTask(false);

        CheckCookieTask checkCookieTask = new CheckCookieTask(config);
        checkCookieTask.run();
    }
}