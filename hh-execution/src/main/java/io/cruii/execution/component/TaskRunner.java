package io.cruii.execution.component;

import io.cruii.pojo.entity.TaskConfigDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@Component
@Slf4j
public class TaskRunner {

    private final ThreadPoolTaskExecutor taskExecutor;


    public TaskRunner(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void run(TaskConfigDO taskConfigDO, BiliTaskListener listener) {
        BiliTask biliTask = new BiliTask(taskConfigDO, listener);
        taskExecutor.execute(biliTask);
    }
}