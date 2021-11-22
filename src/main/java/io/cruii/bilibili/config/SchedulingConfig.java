package io.cruii.bilibili.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 定时任务配置
 *
 * @author cruii
 * Created on 2021/11/22
 */
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(
                new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2)
        );
    }
}
