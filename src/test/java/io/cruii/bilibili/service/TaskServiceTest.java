package io.cruii.bilibili.service;

import io.cruii.bilibili.dto.TaskConfigDTO;
import io.cruii.bilibili.entity.BilibiliUserInfo;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author cruii
 * Created on 2021/9/14
 */
@SpringBootTest
@Log4j2
@RunWith(SpringRunner.class)
class TaskServiceTest {
    @Resource
    private TaskService taskService;

    @Test
    void createNewTask() throws InterruptedException {
        TaskConfigDTO config = new TaskConfigDTO();
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

        boolean created = taskService.createNewTask(config);
        Assertions.assertTrue(created, "创建任务失败，请检查Cookie有效性");
        Thread.sleep(3000);
    }
}