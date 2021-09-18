package io.cruii.bilibili.service.impl;

import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.dto.TaskConfigDTO;
import io.cruii.bilibili.entity.BilibiliUserInfo;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.repository.BilibiliUserInfoRepository;
import io.cruii.bilibili.repository.TaskConfigRepository;
import io.cruii.bilibili.service.TaskService;
import io.cruii.bilibili.task.BilibiliTaskExecutor;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Executor;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Service
@Log4j2
public class TaskServiceImpl implements TaskService {

    private final Executor bilibiliExecutor;

    private final TaskConfigRepository taskConfigRepository;

    private final BilibiliUserInfoRepository bilibiliUserInfoRepository;

    private final MapperFactory mapperFactory;

    public TaskServiceImpl(Executor bilibiliExecutor,
                           TaskConfigRepository taskConfigRepository,
                           BilibiliUserInfoRepository bilibiliUserInfoRepository,
                           MapperFactory mapperFactory) {
        this.bilibiliExecutor = bilibiliExecutor;
        this.taskConfigRepository = taskConfigRepository;
        this.bilibiliUserInfoRepository = bilibiliUserInfoRepository;
        this.mapperFactory = mapperFactory;
    }

    /**
     * 锁对象
     */
    private final Object lock = new Object();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createNewTask(TaskConfigDTO taskConfig) {
        TaskConfig config = mapperFactory.getMapperFacade().map(taskConfig, TaskConfig.class);
        BilibiliDelegate bilibiliDelegate = new BilibiliDelegate(config);
        // 验证并获取用户B站信息
        BilibiliUserInfo user = bilibiliDelegate.getUser();

        if (Boolean.TRUE.equals(user.getIsLogin())) {
            // 用户Cookie有效
            // 持久化任务配置信息
            taskConfigRepository.save(config);

            // 持久化用户信息
            bilibiliUserInfoRepository.save(user);

            // 初次执行任务
            bilibiliExecutor.execute(() -> new BilibiliTaskExecutor(config).execute());
        }

        return user.getIsLogin();
    }
}