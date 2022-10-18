package io.cruii.execution.component;

import io.cruii.component.BilibiliDelegate;
import io.cruii.component.TaskExecutor;
import io.cruii.context.BilibiliUserContext;
import io.cruii.pojo.po.BilibiliUser;
import io.cruii.pojo.po.TaskConfig;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@Component
public class TaskRunner {

    private final ThreadPoolTaskExecutor taskExecutor;

    public TaskRunner(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void run(TaskConfig taskConfig) {
        taskExecutor.execute(() -> {
            BilibiliDelegate delegate = new BilibiliDelegate(taskConfig);
            BilibiliUser user = delegate.getUser();
            BilibiliUserContext.set(user);
            TaskExecutor executor = new TaskExecutor(delegate);
            try {
                BilibiliUser retUser = executor.execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
