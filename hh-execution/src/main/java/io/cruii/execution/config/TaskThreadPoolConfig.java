package io.cruii.execution.config;

import io.cruii.execution.component.BiliTaskExecutor;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author cruii
 * Created on 2022/4/6
 */
@Configuration
@Data
public class TaskThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor taskExecutor() {
        ThreadPoolExecutor executor = new BiliTaskExecutor(1,1, 10 , TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(3000), "bili-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }
}
