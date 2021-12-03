package io.cruii.bilibili.component;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpException;
import cn.hutool.json.JSONObject;
import com.github.rholder.retry.*;
import io.cruii.bilibili.context.BilibiliUserContext;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.exception.BilibiliCookieExpiredException;
import io.cruii.bilibili.task.*;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author cruii
 * Created on 2021/9/15
 */
@Log4j2
public class TaskExecutor {
    private final List<Task> taskList = new ArrayList<>();
    private final BilibiliDelegate delegate;
    private static final ThreadPoolTaskExecutor PUSH_EXECUTOR;

    static {
        ThreadPoolTaskExecutor executor = new TaskThreadPoolExecutor();
        // 核心线程数：线程池创建时候初始化的线程数
        executor.setCorePoolSize(50);
        // 最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(50);
        // 缓冲队列：用来缓冲执行任务的队列
        executor.setQueueCapacity(500);
        // 允许线程的空闲时间60秒：当超过了核心线程之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(120);
        // 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix("push-");
        // 缓冲队列满了之后的拒绝策略：由调用线程处理（一般是主线程）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        PUSH_EXECUTOR = executor;
    }

    public TaskExecutor(BilibiliDelegate delegate) {
        this.delegate = delegate;
        taskList.add(new WatchVideoTask(delegate));
        taskList.add(new MangaTask(delegate));
        taskList.add(new DonateCoinTask(delegate));
        taskList.add(new Silver2CoinTask(delegate));
        taskList.add(new LiveCheckIn(delegate));
        taskList.add(new DonateGiftTask(delegate));
        taskList.add(new ChargeTask(delegate));
        taskList.add(new GetVipPrivilegeTask(delegate));
        taskList.add(new ReadMangaTask(delegate));
    }

    public BilibiliUser execute() {
        Collections.shuffle(taskList);
        taskList.add(new GetCoinChangeLogTask(delegate));
        taskList.add(new CheckCookieTask(delegate));
        Collections.reverse(taskList);

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfExceptionOfType(HttpException.class)
                .retryIfExceptionOfType(IORuntimeException.class)
                .withStopStrategy(StopStrategies.stopAfterAttempt(12))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        if (attempt.hasException()) {
                            log.error("第{}次调用失败: {}, 进行重试", attempt.getAttemptNumber(), attempt.getExceptionCause().getMessage());
                        }
                    }
                })
                .build();
        boolean expired = false;
        for (Task task : taskList) {
            try {
                log.info("[{}]", task.getName());
                retryer.call(() -> {
                    task.run();
                    return true;
                });
                TimeUnit.SECONDS.sleep(3L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("重试[{}]任务失败, {}", task.getName(), e.getMessage());
                if (e.getCause() instanceof BilibiliCookieExpiredException) {
                    expired = true;
                    break;
                }
            } catch (RetryException e) {
                log.error("[{}]任务超过执行次数, {}", task.getName(), e.getMessage());
            }
        }

        BilibiliUser user = BilibiliUserContext.get();

        if (!expired) {
            log.info("[所有任务已执行完成]");

            user = calExp();
        }

        Boolean result = push();

        log.info("账号[{}]推送结果: {}", user.getDedeuserid(), result);

        BilibiliUserContext.remove();

        return user;
    }

    private Boolean push() {
        Future<Boolean> future = PUSH_EXECUTOR.submit(new PushTask(MDC.get("traceId"), delegate));
        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return false;
    }

    private BilibiliUser calExp() {
        BilibiliUser user = delegate.getUser();
        int exp = 0;
        // 获取当日获取的经验
        JSONObject expRewardStatus = delegate.getExpRewardStatus();
        JSONObject body = expRewardStatus.getJSONObject("data");
        Boolean share = body.getBool("share", false);
        Boolean watch = body.getBool("watch", false);
        Boolean login = body.getBool("login", false);
        Integer coinExp = body.getInt("coins", 0);

        if (Boolean.TRUE.equals(share)) {
            exp += 5;
        }

        if (Boolean.TRUE.equals(watch)) {
            exp += 5;
        }

        if (Boolean.TRUE.equals(login)) {
            exp += 5;
        }

        exp += coinExp;
        if (user.getLevel() < 6) {
            int diff = user.getNextExp() - user.getCurrentExp();

            int days = (diff / exp) + 1;
            user.setUpgradeDays(days);
            if (diff <= exp) {
                user.setUpgradeDays(null);
            }
        } else {
            user.setUpgradeDays(null);
        }
        log.info("今日已获得[{}]点经验", exp);

        if (user.getLevel() < 6) {
            int upgradeDays = (user.getNextExp() - user.getCurrentExp()) / exp;
            log.info("按照当前进度，升级到Lv{}还需要: {}天", user.getLevel() + 1, upgradeDays + 1);
        } else {
            log.info("当前等级Lv6，经验值为：{}", user.getCurrentExp());
        }

        return user;
    }
}
