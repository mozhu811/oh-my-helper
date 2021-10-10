package io.cruii.bilibili.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.component.TaskRunner;
import io.cruii.bilibili.dto.TaskConfigDTO;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.mapper.BilibiliUserMapper;
import io.cruii.bilibili.mapper.TaskConfigMapper;
import io.cruii.bilibili.service.TaskService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Service
@Log4j2
public class TaskServiceImpl implements TaskService {

    private final TaskConfigMapper taskConfigMapper;

    private final BilibiliUserMapper bilibiliUserMapper;

    private final MapperFactory mapperFactory;

    public TaskServiceImpl(TaskConfigMapper taskConfigMapper,
                           BilibiliUserMapper bilibiliUserMapper,
                           MapperFactory mapperFactory) {
        this.taskConfigMapper = taskConfigMapper;
        this.bilibiliUserMapper = bilibiliUserMapper;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public boolean createTask(TaskConfigDTO taskConfig) throws InterruptedException {
        TaskConfig config = mapperFactory.getMapperFacade().map(taskConfig, TaskConfig.class);
        BilibiliDelegate delegate = new BilibiliDelegate(config.getDedeuserid(), config.getSessdata(), config.getBiliJct());
        // 验证并获取用户B站信息
        BilibiliUser user = delegate.getUser();

        if (Boolean.TRUE.equals(user.getIsLogin())) {
            // 用户Cookie有效
            // 持久化任务配置信息
            TaskConfig existConfig = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class).eq(TaskConfig::getDedeuserid, user.getDedeuserid()));
            if (Objects.nonNull(existConfig)) {
                config.setId(existConfig.getId());
                taskConfigMapper.updateById(config);
            } else {
                taskConfigMapper.insert(config);
            }

            // 持久化用户信息
            BilibiliUser existUser = bilibiliUserMapper.selectOne(Wrappers.lambdaQuery(BilibiliUser.class).eq(BilibiliUser::getDedeuserid, user.getDedeuserid()));
            if (Objects.isNull(existUser)) {
                bilibiliUserMapper.insert(user);
            } else {
                user.setId(existUser.getId());
                bilibiliUserMapper.updateById(user);
            }

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
        return Objects.nonNull(taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class).eq(TaskConfig::getDedeuserid, dedeuserid)));
    }

    @Override
    public void removeTask(String dedeuserid) {
        BilibiliUser existUser = bilibiliUserMapper.selectOne(Wrappers.lambdaQuery(BilibiliUser.class).eq(BilibiliUser::getDedeuserid, dedeuserid));
        TaskConfig existConfig = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class).eq(TaskConfig::getDedeuserid, dedeuserid));

        taskConfigMapper.deleteById(existConfig);
        bilibiliUserMapper.deleteById(existUser);
    }

    @Override
    public TaskConfig getTask(String dedeuserId, String sessdata, String biliJct) {

        return taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class)
                .eq(TaskConfig::getDedeuserid, dedeuserId)
                .eq(TaskConfig::getSessdata, sessdata)
                .eq(TaskConfig::getBiliJct, biliJct));
    }
}
