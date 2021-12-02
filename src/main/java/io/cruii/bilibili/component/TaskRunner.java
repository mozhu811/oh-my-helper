package io.cruii.bilibili.component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.bilibili.context.BilibiliUserContext;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.mapper.BilibiliUserMapper;
import io.cruii.bilibili.mapper.TaskConfigMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author cruii
 * Created on 2021/9/24
 */
@Component
@PropertySource(value = "classpath:application.yml")
@Log4j2
public class TaskRunner {
    private static final BlockingQueue<String> TASK_QUEUE = new LinkedBlockingDeque<>();

    private final TaskConfigMapper taskConfigMapper;
    private final BilibiliUserMapper bilibiliUserMapper;
    private final Executor bilibiliExecutor;

    public TaskRunner(TaskConfigMapper taskConfigMapper,
                      BilibiliUserMapper bilibiliUserMapper,
                      Executor bilibiliExecutor) {
        this.taskConfigMapper = taskConfigMapper;
        this.bilibiliUserMapper = bilibiliUserMapper;
        this.bilibiliExecutor = bilibiliExecutor;
        startTaskThread();
    }

    public static BlockingQueue<String> getTaskQueue() {
        return TASK_QUEUE;
    }

    private void startTaskThread() {
        log.info("开启任务线程");
        new Thread(() -> {
            while (true) {
                try {
                    String uid = TASK_QUEUE.take();
                    run(uid);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }

            }
        }).start();
    }

    public void run(String uid) {
        TaskConfig taskConfig = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class).eq(TaskConfig::getDedeuserid, uid));
        Optional.ofNullable(taskConfig)
                .ifPresent(this::accept);
    }

    @Scheduled(cron = "${task.cron:0 10 0 * * ?}")
    public void run() {
        taskConfigMapper
                .selectList(null)
                .forEach(this::accept);
    }

    private void accept(TaskConfig config) {
        bilibiliExecutor.execute(() -> {
            BilibiliDelegate delegate = new BilibiliDelegate(config);
            BilibiliUser user = delegate.getUser();
            BilibiliUserContext.set(user);

            TaskExecutor taskExecutor = new TaskExecutor(delegate);
            user = taskExecutor.execute();
            user.setLastRunTime(LocalDateTime.now());
            bilibiliUserMapper.updateById(user);
        });
    }
}
