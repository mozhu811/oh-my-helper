package io.cruii.execution.config;

import io.cruii.component.TaskThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author cruii
 * Created on 2022/4/6
 */
@Configuration
public class TreadPoolConfig {
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new TaskThreadPoolExecutor();
        // 核心线程数：线程池创建时候初始化的线程数
        executor.setCorePoolSize(8);
        // 最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(8);
        // 缓冲队列：用来缓冲执行任务的队列
        executor.setQueueCapacity(Integer.MAX_VALUE);
        // 允许线程的空闲时间60秒：当超过了核心线程之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(10);
        // 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix("task-");
        // 缓冲队列满了之后的拒绝策略：当缓冲队列满了，新任务进来的时候有三种策略：1、直接丢弃 2、保留最老的任务 3、保留最老的任务并且执行新任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();

        return executor;
    }
}
