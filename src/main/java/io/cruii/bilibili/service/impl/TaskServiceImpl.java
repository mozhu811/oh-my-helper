package io.cruii.bilibili.service.impl;

import cn.hutool.json.JSONObject;
import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.component.TaskRunner;
import io.cruii.bilibili.dao.BilibiliUserRepository;
import io.cruii.bilibili.dao.TaskConfigRepository;
import io.cruii.bilibili.dto.TaskConfigDTO;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.service.TaskService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Service
@Log4j2
public class TaskServiceImpl implements TaskService {

    private final TaskConfigRepository taskConfigRepository;

    private final BilibiliUserRepository bilibiliUserRepository;

    private final MapperFactory mapperFactory;

    public TaskServiceImpl(TaskConfigRepository taskConfigRepository,
                           BilibiliUserRepository bilibiliUserRepository,
                           MapperFactory mapperFactory) {
        this.taskConfigRepository = taskConfigRepository;
        this.bilibiliUserRepository = bilibiliUserRepository;
        this.mapperFactory = mapperFactory;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTask(TaskConfigDTO taskConfig) throws InterruptedException {
        TaskConfig config = mapperFactory.getMapperFacade().map(taskConfig, TaskConfig.class);
        BilibiliDelegate delegate = new BilibiliDelegate(config.getDedeuserid(), config.getSessdata(), config.getBiliJct(), config.getUserAgent());
        // 验证并获取用户B站信息
        BilibiliUser user = delegate.getUser();

        if (Boolean.TRUE.equals(user.getIsLogin())) {
            // 用户Cookie有效
            // 持久化任务配置信息
            taskConfigRepository.findOne(user.getDedeuserid())
                    .ifPresent(exist -> config.setId(exist.getId()));
            taskConfigRepository.saveAndFlush(config);

            // 持久化用户信息
            bilibiliUserRepository.findOne(user.getDedeuserid())
                            .ifPresent(exist -> user.setId(exist.getId()));
            bilibiliUserRepository.saveAndFlush(user);

            // 初次执行任务
            TaskRunner.getTaskQueue().put(user.getDedeuserid());
            if (Boolean.TRUE.equals(config.getFollowDeveloper())) {
                String devUid = "287969457";
                JSONObject followResp = delegate.followUser(devUid);
                if (followResp.getInt("code") == 0) {
                    log.info("关注账号[{}]成功", devUid);
                } else {
                    log.error("关注失败");
                }
            }
        }

        return user.getIsLogin();
    }

    @Override
    public boolean isExist(String dedeuserid) {
        return taskConfigRepository.findOne(dedeuserid).isPresent();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTask(String dedeuserid) {
        taskConfigRepository
                .findOne(dedeuserid)
                .ifPresent(taskConfigRepository::delete);

        bilibiliUserRepository
                .findOne(dedeuserid)
                .ifPresent(bilibiliUserRepository::delete);
    }
}
