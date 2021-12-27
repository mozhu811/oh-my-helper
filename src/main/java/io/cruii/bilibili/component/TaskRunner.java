package io.cruii.bilibili.component;

import io.cruii.bilibili.context.BilibiliUserContext;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.mapper.BilibiliUserMapper;
import io.cruii.bilibili.mapper.TaskConfigMapper;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
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
    private final BilibiliUserMapper bilibiliUserMapper;
    private final TaskManager taskManager;

    public TaskRunner(TaskConfigMapper taskConfigMapper,
                      BilibiliUserMapper bilibiliUserMapper,
                      TaskManager taskManager) {
        this.taskConfigMapper = taskConfigMapper;
        this.bilibiliUserMapper = bilibiliUserMapper;
        this.taskManager = taskManager;
    }

    @Scheduled(cron = "${task.cron:0 10 0 * * ?}")
    public void run() {
        List<TaskConfig> taskConfigs = taskConfigMapper
                .selectList(null);
        taskManager.putAll(taskConfigs);
    }

    @PostConstruct
    public void init() {
        log.debug("初始化线程池");
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
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
            });
            thread.setDaemon(true);
            thread.start();
        }
    }
}
