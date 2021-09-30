package io.cruii.bilibili.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.push.QyWechatPusher;
import io.cruii.bilibili.repository.BilibiliUserRepository;
import io.cruii.bilibili.repository.TaskConfigRepository;
import io.cruii.bilibili.service.BilibiliUserService;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
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
    private final BilibiliUserRepository bilibiliUserRepository;
    private final Executor bilibiliExecutor;
    private final BilibiliUserService userService;

    public TaskRunner(TaskConfigRepository taskConfigRepository,
                      BilibiliUserRepository bilibiliUserRepository,
                      Executor bilibiliExecutor,
                      BilibiliUserService userService) {
        this.taskConfigRepository = taskConfigRepository;
        this.bilibiliUserRepository = bilibiliUserRepository;
        this.bilibiliExecutor = bilibiliExecutor;
        this.userService = userService;
    }

    public void run() {
        new Thread(() -> {
            try {
                while (true) {
                    String traceId = FINISH_QUEUE.take();
                    TaskConfig taskConfig = CACHE.get(traceId);

                    String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
                    List<String> logs = FileUtil.readLines(new File("logs/all-" + date + ".log"), StandardCharsets.UTF_8);
                    String content = logs
                            .stream()
                            .filter(line -> line.contains(traceId))
                            .map(line -> line.split("\\|\\|")[1])
                            .collect(Collectors.joining("<br>"));

                    bilibiliUserRepository
                            .findById(taskConfig.getDedeuserid())
                            .ifPresent(user -> logs.stream()
                                    .filter(line -> line.contains("按照当前进度"))
                                    .map(line -> line.substring(line.lastIndexOf(":") + 1, line.length() - 1).trim())
                                    .findFirst()
                                    .ifPresent(upgradeDays -> {
                                        user.setUpgradeDays(Integer.valueOf(upgradeDays));
                                        bilibiliUserRepository.save(user);
                                    })
                            );

                    userService.saveAndUpdate(taskConfig.getDedeuserid(), taskConfig.getSessdata(), taskConfig.getBiliJct());
                    String corpId = taskConfig.getCorpId();
                    String corpSecret = taskConfig.getCorpSecret();
                    String agentId = taskConfig.getAgentId();
                    String mediaId = taskConfig.getMediaId();
                    if (!CharSequenceUtil.hasBlank(corpId, corpSecret, agentId, mediaId)) {
                        QyWechatPusher pusher = new QyWechatPusher(corpId, corpSecret, agentId, mediaId);
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
                    new TaskExecutor(config).execute();
                }));
    }
}
