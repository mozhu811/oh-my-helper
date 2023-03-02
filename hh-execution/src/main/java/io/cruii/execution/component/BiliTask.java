package io.cruii.execution.component;

import io.cruii.component.BilibiliDelegate;
import io.cruii.component.TaskExecutor;
import io.cruii.context.BilibiliUserContext;
import io.cruii.model.BiliUser;
import io.cruii.model.custom.BiliTaskResult;
import io.cruii.pojo.entity.TaskConfigDO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Optional;

@Slf4j
public class BiliTask implements Runnable {
    @Getter
    private final TaskConfigDO config;

    private final BiliTaskListener listener;

    public BiliTask(TaskConfigDO config, BiliTaskListener listener) {
        this.config = config;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            BilibiliDelegate delegate = new BilibiliDelegate(config);
            Optional<BiliUser> user = Optional.ofNullable(delegate.getUserDetails());
            user.ifPresent(u -> {
                BilibiliUserContext.set(u);
                TaskExecutor executor = new TaskExecutor(delegate);
                BiliTaskResult result = executor.execute();
                BilibiliUserContext.remove();
                listener.onCompletion(result);
            });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error("执行任务发生异常", e);
        } finally {
            MDC.clear();
        }
    }
}
