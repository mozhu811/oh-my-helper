package io.cruii.bilibili.task;

import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;

/**
 * @author cruii
 * Created on 2021/9/15
 */
@Log4j2
public abstract class AbstractTask implements Task {
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String INVALID_ARGUMENT = "invalid_argument";
    public static final String MANGA_CLOCK_IN_DUPLICATE = "clockin clockin is duplicate";

    public final TaskConfig config;
    public final BilibiliDelegate delegate;

    AbstractTask(TaskConfig config) {
        this.config = config;
        this.delegate = new BilibiliDelegate(config.getDedeuserid(), config.getSessdata(), config.getBiliJct(), config.getUserAgent());
    }
}
