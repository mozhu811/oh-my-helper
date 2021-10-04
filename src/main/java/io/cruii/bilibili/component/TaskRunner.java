package io.cruii.bilibili.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.cruii.bilibili.dao.BilibiliUserRepository;
import io.cruii.bilibili.dao.TaskConfigRepository;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.push.QyWechatPusher;
import io.cruii.bilibili.push.ServerChanPusher;
import io.cruii.bilibili.push.TelegramBotPusher;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
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
    private static final BlockingQueue<String> TASK_QUEUE = new LinkedBlockingDeque<>();
    protected static final BlockingQueue<String> FINISH_QUEUE = new LinkedBlockingDeque<>();

    private static final Map<String, TaskConfig> CACHE = new HashMap<>();

    private final TaskConfigRepository taskConfigRepository;
    private final BilibiliUserRepository bilibiliUserRepository;
    private final Executor bilibiliExecutor;

    public TaskRunner(TaskConfigRepository taskConfigRepository,
                      BilibiliUserRepository bilibiliUserRepository,
                      Executor bilibiliExecutor) {
        this.taskConfigRepository = taskConfigRepository;
        this.bilibiliUserRepository = bilibiliUserRepository;
        this.bilibiliExecutor = bilibiliExecutor;

        startTaskThread();
        startPushThread();
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

    private void startPushThread() {
        log.info("开启推送线程");
        new Thread(() -> {
            try {
                while (true) {
                    String traceId = FINISH_QUEUE.take();
                    TaskConfig taskConfig = CACHE.get(traceId);

                    String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
                    List<String> logs = FileUtil.readLines(new File("logs/all-" + date + ".0.log"), StandardCharsets.UTF_8);
                    String content = logs
                            .stream()
                            .filter(line -> line.contains(traceId))
                            .map(line -> line.split("\\|\\|")[1])
                            .collect(Collectors.joining("\n"));

                    BilibiliDelegate delegate = new BilibiliDelegate(taskConfig.getDedeuserid(), taskConfig.getSessdata(), taskConfig.getBiliJct());
                    BilibiliUser user = delegate.getUser();

                    logs.stream()
                            .filter(line -> line.contains(traceId) && line.contains("当前进度"))
                            .forEach(line -> {
                                String upgradeDays = line.substring(line.lastIndexOf(":") + 1, line.length() - 1).trim();
                                user.setUpgradeDays(Integer.valueOf(upgradeDays));
                                bilibiliUserRepository.saveAndFlush(user);
                            });

                    String corpId = taskConfig.getCorpId();
                    String corpSecret = taskConfig.getCorpSecret();
                    String agentId = taskConfig.getAgentId();
                    String mediaId = taskConfig.getMediaId();

                    if (!CharSequenceUtil.hasBlank(corpId, corpSecret, agentId, mediaId)) {

                        QyWechatPusher pusher = new QyWechatPusher(corpId, corpSecret, agentId, mediaId);
                        pusher.push(content.replace("\n", "<br>"));
                    }

                    String tgBotToken = taskConfig.getTgBotToken();
                    String tgBotChatId = taskConfig.getTgBotChatId();

                    if (!CharSequenceUtil.hasBlank(tgBotToken, tgBotChatId)) {
                        TelegramBotPusher pusher = new TelegramBotPusher(tgBotToken, tgBotChatId);
                        pusher.push(content);
                    }

                    String scKey = taskConfig.getScKey();
                    if (CharSequenceUtil.isNotBlank(scKey)) {
                        ServerChanPusher pusher = new ServerChanPusher(scKey);
                        pusher.push(content);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }, "message-push").start();
    }

    @Scheduled(cron = "0 10 0 * * ?")
    public void run() {
        taskConfigRepository
                .findAll()
                .forEach(config -> bilibiliExecutor.execute(() -> {
                    String traceId = MDC.getCopyOfContextMap().get("traceId");
                    CACHE.put(traceId, config);
                    new TaskExecutor(config).execute();
                }));
    }

    public void run(String uid) {
        taskConfigRepository
                .findOne(uid)
                .ifPresent(config -> bilibiliExecutor.execute(() -> {
                    String traceId = MDC.getCopyOfContextMap().get("traceId");
                    CACHE.put(traceId, config);
                    new TaskExecutor(config).execute();
                }));
    }
}
