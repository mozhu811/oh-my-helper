package io.cruii.execution.config;

import io.cruii.component.TaskThreadPoolExecutor;
import io.cruii.execution.component.BiliTask;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author cruii
 * Created on 2022/4/6
 */
@Configuration
@Data
public class TaskThreadPoolConfig {
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new TaskThreadPoolExecutor();
        // 核心线程数：线程池创建时候初始化的线程数
        executor.setCorePoolSize(10);
        // 最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(50);
        // 缓冲队列：用来缓冲执行任务的队列
        executor.setQueueCapacity(3000);
        // 允许线程的空闲时间60秒：当超过了核心线程之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(10);
        // 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix("bili-task-");
        // 缓冲队列满了之后的拒绝策略：当缓冲队列满了，新任务进来的时候有三种策略：1、直接丢弃 2、保留最老的任务 3、保留最老的任务并且执行新任务
        executor.setRejectedExecutionHandler(new BiliTaskRejectionPolicy());
        executor.initialize();

        return executor;
    }

    static class BiliTaskRejectionPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof BiliTask) {
                String dedeuserid = ((BiliTask) r).getConfig().getDedeuserid();
                for (Runnable queuedTask : executor.getQueue()) {
                    if (queuedTask instanceof BiliTask && Objects.equals(((BiliTask) queuedTask).getConfig().getDedeuserid(), dedeuserid)) {
                        // A task with the same ID is already in the queue, reject the new task
                        throw new RejectedExecutionException("Task " + dedeuserid + " is already in the queue");
                    }
                }
            }
        }
    }
}
