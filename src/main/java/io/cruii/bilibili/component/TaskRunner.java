package io.cruii.bilibili.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.bilibili.context.BilibiliUserContext;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.mapper.BilibiliUserMapper;
import io.cruii.bilibili.mapper.TaskConfigMapper;
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
import java.util.Optional;
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
            while (true) {
                String traceId = getTraceId();
                TaskConfig taskConfig = CACHE.get(traceId);

                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
                List<String> logs = FileUtil.readLines(new File("logs/all-" + date + ".0.log"), StandardCharsets.UTF_8);
                assert traceId != null;
                String content = logs
                        .stream()
                        .filter(line -> line.contains(traceId) && (line.contains("INFO") || line.contains("ERROR")))
                        .map(line -> line.split("\\|\\|")[1])
                        .collect(Collectors.joining("\n"));

                BilibiliDelegate delegate = new BilibiliDelegate(taskConfig.getDedeuserid(), taskConfig.getSessdata(), taskConfig.getBiliJct());
                BilibiliUser user = delegate.getUser();

                if (user.getLevel() < 6) {
                    logs.stream()
                            .filter(line -> line.contains(traceId) && line.contains("当前进度"))
                            .forEach(line -> {
                                String upgradeDays = line.substring(line.lastIndexOf(":") + 1, line.length() - 1).trim();
                                user.setUpgradeDays(Integer.parseInt(upgradeDays));
                            });
                }

                bilibiliUserMapper.update(user, Wrappers.lambdaUpdate(BilibiliUser.class).eq(BilibiliUser::getDedeuserid, user.getDedeuserid()));

                push(taskConfig, content);
            }
        }, "message-push").start();
    }

    private void push(TaskConfig taskConfig, String content) {
        String corpId = taskConfig.getCorpId();
        String corpSecret = taskConfig.getCorpSecret();
        String agentId = taskConfig.getAgentId();
        String mediaId = taskConfig.getMediaId();

        boolean result = false;
        if (!CharSequenceUtil.hasBlank(corpId, corpSecret, agentId, mediaId)) {
            QyWechatPusher pusher = new QyWechatPusher(corpId, corpSecret, agentId, mediaId);
            result = pusher.push(content.replace("\n", "<br>"));
        } else if (!CharSequenceUtil.hasBlank(taskConfig.getTgBotToken(), taskConfig.getTgBotChatId())) {
            TelegramBotPusher pusher = new TelegramBotPusher(taskConfig.getTgBotToken(), taskConfig.getTgBotChatId());
            result = pusher.push(content);
        } else if (CharSequenceUtil.isNotBlank(taskConfig.getScKey())) {
            ServerChanPusher pusher = new ServerChanPusher(taskConfig.getScKey());
            result = pusher.push(content);
        } else {
            log.info("该账号未配置推送或推送配置异常");
        }

        log.info("账号[{}]推送结果: {}", taskConfig.getDedeuserid(), result);
        BilibiliUserContext.remove();
    }

    private String getTraceId() {
        String traceId = null;
        try {
            traceId = FINISH_QUEUE.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return traceId;
    }

    @Scheduled(cron = "0 10 0 * * ?")
    public void run() {
        taskConfigMapper
                .selectList(null)
                .forEach(this::accept);
    }

    public void run(String uid) {
        TaskConfig taskConfig = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class).eq(TaskConfig::getDedeuserid, uid));
        Optional.ofNullable(taskConfig)
                .ifPresent(this::accept);
    }

    private void accept(TaskConfig config) {
        bilibiliExecutor.execute(() -> {
            BilibiliDelegate delegate = new BilibiliDelegate(config.getDedeuserid(), config.getSessdata(), config.getBiliJct(), config.getUserAgent());
            BilibiliUser user = delegate.getUser();
            if (Boolean.TRUE.equals(user.getIsLogin())) {
                BilibiliUserContext.set(user);

                String traceId = MDC.getCopyOfContextMap().get("traceId");
                CACHE.put(traceId, config);
                new TaskExecutor(config).execute();
            } else {
                // todo 推送过期消息
                log.warn("账户[{}]Cookie已过期", user.getDedeuserid());
            }
        });
    }
}
