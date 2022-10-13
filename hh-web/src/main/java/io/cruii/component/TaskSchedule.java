package io.cruii.component;

import io.cruii.mapper.TaskConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author cruii
 * Created on 2022/4/7
 */
@Component
@Slf4j
public class TaskSchedule {
    private final TaskConfigMapper taskConfigMapper;

    public TaskSchedule(TaskConfigMapper taskConfigMapper) {
        this.taskConfigMapper = taskConfigMapper;
    }

    @Scheduled(cron = "${task.cron:0 10 0 * * ?}")
    public void doTask() {
        taskConfigMapper.selectList(null).forEach(taskConfig -> {
        });
    }
}
