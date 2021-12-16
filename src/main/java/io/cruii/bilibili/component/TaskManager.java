package io.cruii.bilibili.component;

import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author cruii
 * Created on 2021/12/15
 */
@Component
@Slf4j
public class TaskManager {
    private final BlockingQueue<TaskConfig> tasks = new LinkedBlockingQueue<>();

    public void put(TaskConfig taskConfig) {
        tasks.add(taskConfig);
    }

    public void putAll(List<TaskConfig> taskConfigs) {
        tasks.addAll(taskConfigs);
    }

    public synchronized TaskConfig get() {
        TaskConfig taskConfig = null;
        try {
            taskConfig = tasks.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        log.debug("当前剩余任务数量：{}", tasks.size());
        return taskConfig;
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    public int size() {
        return tasks.size();
    }
}
