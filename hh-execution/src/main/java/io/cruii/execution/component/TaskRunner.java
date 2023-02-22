package io.cruii.execution.component;

import cn.hutool.core.io.FileUtil;
import io.cruii.component.BilibiliDelegate;
import io.cruii.component.TaskExecutor;
import io.cruii.context.BilibiliUserContext;
import io.cruii.execution.feign.PushFeignService;
import io.cruii.model.BiliUser;
import io.cruii.model.custom.BiliUserAndDays;
import io.cruii.pojo.entity.TaskConfigDO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@Component
@Slf4j
public class TaskRunner {

    private final ThreadPoolTaskExecutor taskExecutor;

    private final PushFeignService pushFeignService;

    private static final BlockingQueue<String> PUSH_QUEUE = new LinkedBlockingQueue<>(3000);

    public TaskRunner(ThreadPoolTaskExecutor taskExecutor,
                      PushFeignService pushFeignService) {
        this.taskExecutor = taskExecutor;
        this.pushFeignService = pushFeignService;

        new Thread(this::pushMessage).start();
    }

    private void pushMessage() {
        while (true) {
            try {
                String msg = PUSH_QUEUE.take();
                String dedeuserid = msg.split(":")[0];
                String traceId = msg.split(":")[1];

                // 日志收集
                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
                File logFile = new File("logs/execution/all-" + date + ".log");
                String content = null;
                if (logFile.exists()) {
                    List<String> logs = FileUtil.readLines(logFile, StandardCharsets.UTF_8);
                    content = logs.stream()
                            .filter(line -> line.contains(traceId) && (line.contains("INFO") || line.contains("ERROR")))
                            .map(line -> line.split("\\|\\|")[1])
                            .collect(Collectors.joining("\n"));
                }
                this.pushFeignService.push(dedeuserid, content);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }
    public BiliUserAndDays run(TaskConfigDO taskConfigDO) {
        AtomicReference<BiliUserAndDays> ret = new AtomicReference<>();
        taskExecutor.execute(() -> {
            try {
                BilibiliDelegate delegate = new BilibiliDelegate(taskConfigDO);
                Optional<BiliUser> user = Optional.ofNullable(delegate.getUserDetails());
                user.ifPresent(u -> {
                    BilibiliUserContext.set(u);
                    TaskExecutor executor = new TaskExecutor(delegate);
                    BiliUserAndDays result = executor.execute();
                    ret.set(result);
                    BilibiliUserContext.remove();
                });
                // 加入到推送队列
                PUSH_QUEUE.put(taskConfigDO.getDedeuserid() + ":" + MDC.get("traceId"));
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                log.error("执行任务发生异常", e);
            } finally {
                MDC.clear();
            }
        });

        return ret.get();
    }
}
