package io.cruii.execution.component;

import io.cruii.pojo.entity.TaskConfigDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@Component
@Slf4j
public class TaskRunner {

    private final ThreadPoolExecutor taskExecutor;


    public TaskRunner(ThreadPoolExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void run(TaskConfigDO taskConfig, BiliTaskListener listener) {
        try {
            BiliTask biliTask = new BiliTask(taskConfig, listener);
            taskExecutor.execute(biliTask);
        } catch (RejectedExecutionException e) {
            log.error("账号[{}]任务被拒绝，原因: {}", taskConfig.getDedeuserid(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("执行账号[{}]任务发生未知异常", taskConfig.getDedeuserid(), e);
        }
    }
}