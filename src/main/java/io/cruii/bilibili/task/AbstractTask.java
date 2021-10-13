package io.cruii.bilibili.task;

import io.cruii.bilibili.component.BilibiliDelegate;
import lombok.Getter;
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

    public final BilibiliDelegate delegate;

    @Getter
    private int attempts = 1;

    AbstractTask(BilibiliDelegate delegate) {
        this.delegate = delegate;
    }

    public void addAttempts() {
        this.attempts++;
    }

    public void checkAttemptsAndChangeProxy() {
        if (attempts % 3 == 0 && attempts <= 9) {
            log.info("切换代理");
            delegate.changeProxy();
        }
    }
}
