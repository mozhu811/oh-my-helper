package io.cruii.bilibili.service.impl;

import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.dto.TaskConfigDTO;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.repository.BilibiliUserRepository;
import io.cruii.bilibili.repository.TaskConfigRepository;
import io.cruii.bilibili.service.TaskService;
import io.cruii.bilibili.component.TaskExecutor;
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

    private final BilibiliUserRepository bilibiliUserRepository;

    private final MapperFactory mapperFactory;

    public TaskServiceImpl(Executor bilibiliExecutor,
                           TaskConfigRepository taskConfigRepository,
                           BilibiliUserRepository bilibiliUserRepository,
                           MapperFactory mapperFactory) {
        this.bilibiliExecutor = bilibiliExecutor;
        this.taskConfigRepository = taskConfigRepository;
        this.bilibiliUserRepository = bilibiliUserRepository;
        this.mapperFactory = mapperFactory;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTask(TaskConfigDTO taskConfig) {
        TaskConfig config = mapperFactory.getMapperFacade().map(taskConfig, TaskConfig.class);
        BilibiliDelegate delegate = new BilibiliDelegate(config);
        // 验证并获取用户B站信息
        BilibiliUser user = delegate.getUser();

        if (Boolean.TRUE.equals(user.getIsLogin())) {
            // 用户Cookie有效
            // 持久化任务配置信息
            taskConfigRepository.save(config);

            // 持久化用户信息
            bilibiliUserRepository.save(user);

            // 初次执行任务
            bilibiliExecutor.execute(() -> new TaskExecutor(config).execute());
        }

        return user.getIsLogin();
    }

    @Override
    public boolean isExist(String dedeuserid) {
        return taskConfigRepository.findById(dedeuserid).isPresent();
    }
}
