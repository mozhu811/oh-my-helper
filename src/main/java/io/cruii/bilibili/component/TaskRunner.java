package io.cruii.bilibili.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.push.QyWechatPusher;
import io.cruii.bilibili.repository.TaskConfigRepository;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/9/24
 */
@Component
@Log4j2
public class TaskRunner {
    private static final Map<String, TaskConfig> CACHE = new HashMap<>();
    protected static final BlockingQueue<String> FINISH_QUEUE = new LinkedBlockingDeque<>();
    private final TaskConfigRepository taskConfigRepository;
    private final Executor bilibiliExecutor;

    public TaskRunner(TaskConfigRepository taskConfigRepository,
                      Executor bilibiliExecutor) {
        this.taskConfigRepository = taskConfigRepository;
        this.bilibiliExecutor = bilibiliExecutor;
    }

    public void run() {
        new Thread(() -> {
            try {
                while (true) {
                    String traceId = FINISH_QUEUE.take();
                    String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
                    String content = FileUtil.readLines(new File("logs/all-" + date + ".log"), StandardCharsets.UTF_8)
                            .stream().filter(line -> line.contains(traceId))
                            .map(line -> line.split("\\|\\|")[1])
                            .collect(Collectors.joining("<br>"));

                    TaskConfig taskConfig = CACHE.get(traceId);

                    String corpId = taskConfig.getCorpId();
                    String corpSecret = taskConfig.getCorpSecret();
                    String agentId = taskConfig.getAgentId();
                    if (!CharSequenceUtil.hasBlank(corpId, corpSecret, agentId)) {
                        QyWechatPusher pusher = new QyWechatPusher(corpId, corpSecret, agentId);
                        pusher.push(content);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }, "message-push").start();

        taskConfigRepository
                .findAll()
                .forEach(config -> bilibiliExecutor.execute(() -> {
                    String traceId = MDC.getCopyOfContextMap().get("traceId");
                    CACHE.put(traceId, config);
                    new BilibiliTaskExecutor(config).execute();
                }));
    }
}
