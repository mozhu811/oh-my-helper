package io.cruii.bilibili.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.context.BilibiliUserContext;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.push.BarkPusher;
import io.cruii.bilibili.push.QyWechatPusher;
import io.cruii.bilibili.push.ServerChanPusher;
import io.cruii.bilibili.push.TelegramBotPusher;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/10/11
 */
@Log4j2
public class PushTask implements Callable<BilibiliUser> {
    private final String traceId;
    private final BilibiliDelegate delegate;

    public PushTask(String traceId,
                    BilibiliDelegate delegate) {
        this.traceId = traceId;
        this.delegate = delegate;
    }

    @Override
    public BilibiliUser call() {
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
        List<String> logs = FileUtil.readLines(new File("logs/all-" + date + ".0.log"), StandardCharsets.UTF_8);
        assert traceId != null;
        String content = logs
                .stream()
                .filter(line -> line.contains(traceId) && (line.contains("INFO") || line.contains("ERROR")))
                .map(line -> line.split("\\|\\|")[1])
                .collect(Collectors.joining("\n"));

        return push(delegate.getConfig(), content);
    }

    private BilibiliUser push(TaskConfig taskConfig, String content) {

        boolean result = false;
        if (CharSequenceUtil.isNotBlank(taskConfig.getBarkToken())) {
            BarkPusher barkPusher = new BarkPusher(taskConfig.getBarkToken());
            result = barkPusher.push(content);
        } else if (!CharSequenceUtil.hasBlank(taskConfig.getCorpId(), taskConfig.getCorpSecret(), taskConfig.getAgentId(), taskConfig.getMediaId())) {
            QyWechatPusher pusher = new QyWechatPusher(taskConfig.getCorpId(), taskConfig.getCorpSecret(), taskConfig.getAgentId(), taskConfig.getMediaId());
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

        return delegate.getUser();
    }
}
