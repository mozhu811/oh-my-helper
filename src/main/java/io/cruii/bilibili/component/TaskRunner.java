package io.cruii.bilibili.component;

import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.mapper.TaskConfigMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/9/24
 */
@Component
@PropertySource(value = "classpath:application.yml")
@Log4j2
public class TaskRunner {

    private final TaskConfigMapper taskConfigMapper;
    private final TaskManager taskManager;

    public TaskRunner(TaskConfigMapper taskConfigMapper,
                      TaskManager taskManager) {
        this.taskConfigMapper = taskConfigMapper;
        this.taskManager = taskManager;
    }

    @Scheduled(cron = "${task.cron:0 10 0 * * ?}")
    public void run() {
        List<TaskConfig> taskConfigs = taskConfigMapper
                .selectList(null);
        taskManager.putAll(taskConfigs);
    }
}
