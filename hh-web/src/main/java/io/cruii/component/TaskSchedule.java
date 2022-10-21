package io.cruii.component;

import io.cruii.handler.ServerHandler;
import io.cruii.mapper.TaskConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author cruii
 * Created on 2022/4/7
 */
@Component
@Slf4j
@RefreshScope
public class TaskSchedule {
    private final TaskConfigMapper taskConfigMapper;

    private final ServerHandler serverHandler;

    public TaskSchedule(TaskConfigMapper taskConfigMapper, ServerHandler serverHandler) {
        this.taskConfigMapper = taskConfigMapper;
        this.serverHandler = serverHandler;
    }

    @Scheduled(cron = "${task.cron:0 0 9 * * ?}")
    public void doTask() {
        taskConfigMapper.selectList(null)
                .forEach(serverHandler::sendMsg);
    }
}
