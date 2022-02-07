package io.cruii.bilibili.component;

import io.cruii.bilibili.context.BilibiliUserContext;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.mapper.BilibiliUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author cruii
 * Created on 2022/2/7
 */
@Component
@Slf4j
public class TaskInitializer implements InitializingBean {
    private final TaskManager taskManager;
    private final BilibiliUserMapper bilibiliUserMapper;

    public TaskInitializer(TaskManager taskManager, BilibiliUserMapper bilibiliUserMapper) {
        this.taskManager = taskManager;
        this.bilibiliUserMapper = bilibiliUserMapper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("初始化线程池");
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                while (true) {
                    TaskConfig taskConfig = taskManager.get();
                    MDC.put("traceId", taskConfig.getDedeuserid());
                    BilibiliDelegate delegate = new BilibiliDelegate(taskConfig);
                    try {
                        BilibiliUser user = delegate.getUser();
                        BilibiliUserContext.set(user);

                        TaskExecutor taskExecutor = new TaskExecutor(delegate);
                        user = taskExecutor.execute();
                        user.setLastRunTime(LocalDateTime.now());
                        bilibiliUserMapper.updateById(user);
                    } catch (Exception e) {
                        log.error("任务执行失败", e);
                    }
                }
            }).start();
        }

    }
}
