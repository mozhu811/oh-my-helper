package io.cruii.bilibili.task;

import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cruii
 * Created on 2021/9/15
 */
@Log4j2
public class BilibiliTaskExecutor {

    private final List<Task> taskList = new ArrayList<>();

    public BilibiliTaskExecutor(TaskConfig taskConfig) {
        taskList.add(new CheckCookieTask(taskConfig));
        taskList.add(new GetCoinChangeLogTask(taskConfig));
    }

    public void execute() {
        log.info("======开始执行任务======");
        taskList.forEach(task -> {
            log.info("======执行[{}]开始======", task.getName());
            task.run();
            log.info("======执行[{}]结束======", task.getName());
        });
    }
}
