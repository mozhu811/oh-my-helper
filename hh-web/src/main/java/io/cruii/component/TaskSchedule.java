package io.cruii.component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.handler.ServerHandler;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.pojo.po.TaskConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Wrapper;

/**
 * @author cruii
 * Created on 2022/4/7
 */
@Component
@Slf4j
public class TaskSchedule {
    private final TaskConfigMapper taskConfigMapper;

    private final ServerHandler serverHandler;

    public TaskSchedule(TaskConfigMapper taskConfigMapper, ServerHandler serverHandler) {
        this.taskConfigMapper = taskConfigMapper;
        this.serverHandler = serverHandler;
    }

    //@Scheduled(cron = "${task.cron:0 20 17 * * ?}")
    @Scheduled(cron = "0 24 17 * * ?")
    public void doTask() {
        taskConfigMapper.selectList(Wrappers.lambdaQuery(TaskConfig.class)
                        .eq(TaskConfig::getDedeuserid, "287969457"))
                .forEach(serverHandler::sendMsg);
    }
}
