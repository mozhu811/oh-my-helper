package io.cruii.component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.handler.ServerHandler;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.pojo.po.BilibiliUser;
import io.cruii.pojo.po.TaskConfig;
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

    public TaskSchedule(TaskConfigMapper taskConfigMapper, BilibiliUserMapper bilibiliUserMapper, ServerHandler serverHandler) {
        this.taskConfigMapper = taskConfigMapper;
        this.bilibiliUserMapper = bilibiliUserMapper;
        this.serverHandler = serverHandler;
    }

    @Scheduled(cron = "${task.cron:0 0/5 * * * ?}")
    public void doTask() {
        List<String> notRunUsers = bilibiliUserMapper.listNotRunUser().stream()
                .map(BilibiliUser::getDedeuserid)
                .limit(10).collect(Collectors.toList());
        if (notRunUsers.isEmpty()) {
            return;
        }
        log.debug(notRunUsers);
        List<TaskConfig> taskConfigs = taskConfigMapper.selectList(Wrappers.lambdaQuery(TaskConfig.class).in(TaskConfig::getDedeuserid, notRunUsers));
        log.debug(taskConfigs);
        taskConfigs
                .forEach(serverHandler::sendMsg);
    }
}
