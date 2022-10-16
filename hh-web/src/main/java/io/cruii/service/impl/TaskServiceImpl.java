package io.cruii.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.component.BilibiliDelegate;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.mapper.PushConfigMapper;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.pojo.dto.PushConfigDTO;
import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.pojo.po.BilibiliUser;
import io.cruii.pojo.po.PushConfig;
import io.cruii.pojo.po.TaskConfig;
import io.cruii.service.TaskService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Service
@Log4j2
@Transactional(rollbackFor = Exception.class)
public class TaskServiceImpl implements TaskService {

    private final TaskConfigMapper taskConfigMapper;

    private final PushConfigMapper pushConfigMapper;

    private final BilibiliUserMapper bilibiliUserMapper;

    private final MapperFactory mapperFactory;

    public TaskServiceImpl(TaskConfigMapper taskConfigMapper,
                           PushConfigMapper pushConfigMapper,
                           BilibiliUserMapper bilibiliUserMapper,
                           MapperFactory mapperFactory) {
        this.taskConfigMapper = taskConfigMapper;
        this.pushConfigMapper = pushConfigMapper;
        this.bilibiliUserMapper = bilibiliUserMapper;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public TaskConfigDTO createTask(TaskConfigDTO taskConfigDTO, PushConfigDTO pushConfigDTO){
        TaskConfig taskConfig = mapperFactory.getMapperFacade().map(taskConfigDTO, TaskConfig.class);
        BilibiliDelegate delegate = new BilibiliDelegate(taskConfig.getDedeuserid(), taskConfig.getSessdata(), taskConfig.getBiliJct());
        // 验证并获取用户B站信息
        BilibiliUser user = delegate.getUser();

        if (Boolean.TRUE.equals(user.getIsLogin())) {
            // 用户Cookie有效
            // 持久化任务配置信息
            TaskConfig existTaskConfig = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class).eq(TaskConfig::getDedeuserid, user.getDedeuserid()));
            if (Objects.nonNull(existTaskConfig)) {
                taskConfig.setId(existTaskConfig.getId());
                taskConfigMapper.updateById(taskConfig);
            } else {
                taskConfigMapper.insert(taskConfig);
            }

            // 持久化推送配置信息
            if (Objects.nonNull(pushConfigDTO)) {
                PushConfig pushConfig = mapperFactory.getMapperFacade().map(pushConfigDTO, PushConfig.class);
                PushConfig existPushConfig = pushConfigMapper.selectOne(Wrappers.lambdaQuery(PushConfig.class).eq(PushConfig::getDedeuserid, user.getDedeuserid()));

                if (Objects.nonNull(existPushConfig)) {
                    pushConfig.setId(existPushConfig.getId());
                    pushConfig.setDedeuserid(user.getDedeuserid());
                    pushConfigMapper.updateById(pushConfig);
                } else {
                    pushConfig.setDedeuserid(user.getDedeuserid());
                    pushConfigMapper.insert(pushConfig);
                }
            }


            // 持久化用户信息
            BilibiliUser existUser = bilibiliUserMapper.selectOne(Wrappers.lambdaQuery(BilibiliUser.class).eq(BilibiliUser::getDedeuserid, user.getDedeuserid()));
            if (Objects.isNull(existUser)) {
                bilibiliUserMapper.insert(user);
            } else {
                user.setId(existUser.getId());
                bilibiliUserMapper.updateById(user);
            }

            if (Boolean.TRUE.equals(taskConfig.getFollowDeveloper())) {
                String devUid = "287969457";
                JSONObject followResp = delegate.followUser(devUid);
                if (followResp.getInt("code") == 0) {
                    log.info("关注账号[{}]成功", devUid);
                } else {
                    log.error("关注失败");
                }
            }
        }

        return mapperFactory.getMapperFacade()
                .map(taskConfig, TaskConfigDTO.class);
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

    @Override
    public TaskConfig getTask(String dedeuserId) {
        return taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class)
                .eq(TaskConfig::getDedeuserid, dedeuserId));
    }

    @Override
    public void updateCookie(String dedeuserid, String sessdata, String biliJct) {
        TaskConfig taskConfig = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class)
                .eq(TaskConfig::getDedeuserid, dedeuserid));
        if (taskConfig != null) {
            taskConfig.setSessdata(sessdata);
            taskConfig.setBiliJct(biliJct);
            taskConfigMapper.updateById(taskConfig);
        }
    }
}
