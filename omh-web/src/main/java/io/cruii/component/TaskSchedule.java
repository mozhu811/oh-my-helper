package io.cruii.component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.feign.PushFeignService;
import io.cruii.handler.ServerHandler;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.pojo.entity.BiliTaskUserDO;
import io.cruii.pojo.entity.TaskConfigDO;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2022/4/7
 */
@Component
@Log4j2
@RefreshScope
public class TaskSchedule {
    private final TaskConfigMapper taskConfigMapper;

    private final BilibiliUserMapper bilibiliUserMapper;

    private final ServerHandler serverHandler;

    private final PushFeignService pushFeignService;
    public TaskSchedule(TaskConfigMapper taskConfigMapper, BilibiliUserMapper bilibiliUserMapper, ServerHandler serverHandler,
                        PushFeignService pushFeignService) {
        this.taskConfigMapper = taskConfigMapper;
        this.bilibiliUserMapper = bilibiliUserMapper;
        this.serverHandler = serverHandler;
        this.pushFeignService = pushFeignService;
    }

    /**
     * 0 0/30 9-17 * * ?    早上9点至下午17点内每30分钟一次
     * 0 0/5 * * * ?        每隔5分钟执行一次
     */
    //@Scheduled(initialDelayString = "${task.initial-delay:30000}", fixedRateString = "${task.fixed-rate:7200000}")
    @Scheduled(cron = "0 0 9-23/1 * * ?")
    public void doTask() {
        List<String> notRunUsers = bilibiliUserMapper
                .listNotRunUser().stream()
                .filter(BiliTaskUserDO::getIsLogin)
                .map(BiliTaskUserDO::getDedeuserid)
                .collect(Collectors.toList());
        if (notRunUsers.isEmpty()) {
            return;
        }
        log.debug("Not run users : {}", notRunUsers);
        List<TaskConfigDO> taskConfigs = taskConfigMapper.selectList(Wrappers.lambdaQuery(TaskConfigDO.class).in(TaskConfigDO::getDedeuserid, notRunUsers));
        taskConfigs
                .forEach(taskConfig -> {
                    String sessdata = taskConfig.getSessdata();
                    sessdata = sessdata.replace(",", "%2C");
                    sessdata = sessdata.replace("*", "%2A");
                    taskConfig.setSessdata(sessdata);
                    serverHandler.sendMsg(taskConfig);
                });
    }

    @Scheduled(cron = "${notify-expired-expression:0 0 9 * * ?}")
    public void notifyExpired() {
        List<String> expiredBiliIdList = bilibiliUserMapper.selectList(Wrappers.lambdaQuery(BiliTaskUserDO.class).eq(BiliTaskUserDO::getIsLogin, false))
                .stream().map(BiliTaskUserDO::getDedeuserid)
                .collect(Collectors.toList());
        this.pushFeignService.notifyExpired(expiredBiliIdList);
    }

}
