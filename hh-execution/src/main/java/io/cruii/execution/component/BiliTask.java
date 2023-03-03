package io.cruii.execution.component;

import io.cruii.component.BilibiliDelegate;
import io.cruii.context.BilibiliUserContext;
import io.cruii.exception.BilibiliCookieExpiredException;
import io.cruii.execution.constant.TaskStatus;
import io.cruii.model.BiliUser;
import io.cruii.model.custom.BiliTaskResult;
import io.cruii.pojo.entity.TaskConfigDO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class BiliTask implements Runnable {
    @Getter
    private final TaskConfigDO config;

    private final BiliTaskListener listener;

    public BiliTask(TaskConfigDO config, BiliTaskListener listener) {
        this.config = config;
        this.listener = listener;
    }

    @Override
    public void run() {
        BilibiliDelegate delegate = new BilibiliDelegate(config);
        AtomicReference<BiliTaskResult> result = new AtomicReference<>();
        try {
            String tid = UUID.randomUUID().toString();
            MDC.put("traceId", tid);
            Optional<BiliUser> user = Optional.ofNullable(delegate.getUserDetails());
            user.ifPresent(u -> {
                BilibiliUserContext.set(u);
                TaskExecutor executor = new TaskExecutor(delegate);
                result.set(executor.execute());
            });
        } catch (BilibiliCookieExpiredException e) {
            log.error("账号[{}]登录失败，请访问 https://ohmyhelper.com/bilibili/ 重新扫码登陆更新Cookie ❌", config.getDedeuserid());
            BiliTaskResult failResult = new BiliTaskResult(TaskStatus.FAIL, delegate.getSpaceAccInfo(config.getDedeuserid()), null);
            result.set(failResult);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            BilibiliUserContext.remove();
            listener.onCompletion(result.get());
            MDC.clear();
        }
    }
}
