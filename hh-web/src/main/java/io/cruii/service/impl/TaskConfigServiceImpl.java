package io.cruii.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.component.PushConfigStructMapper;
import io.cruii.component.TaskConfigStructMapper;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.mapper.PushConfigMapper;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.pojo.dto.PushConfigDTO;
import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.pojo.entity.PushConfigDO;
import io.cruii.pojo.entity.TaskConfigDO;
import io.cruii.pojo.vo.TaskConfigVO;
import io.cruii.service.TaskConfigService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Service
@Log4j2
@Transactional(rollbackFor = Exception.class)
public class TaskConfigServiceImpl implements TaskConfigService {

    private final TaskConfigMapper taskConfigMapper;

    private final PushConfigMapper pushConfigMapper;

    private final BilibiliUserMapper bilibiliUserMapper;

    private final TaskConfigStructMapper taskConfigStructMapper;

    private final PushConfigStructMapper pushConfigStructMapper;

    public TaskConfigServiceImpl(TaskConfigMapper taskConfigMapper,
                                 PushConfigMapper pushConfigMapper,
                                 BilibiliUserMapper bilibiliUserMapper,
                                 TaskConfigStructMapper taskConfigStructMapper,
                                 PushConfigStructMapper pushConfigStructMapper) {
        this.taskConfigMapper = taskConfigMapper;
        this.pushConfigMapper = pushConfigMapper;
        this.bilibiliUserMapper = bilibiliUserMapper;
        this.taskConfigStructMapper = taskConfigStructMapper;
        this.pushConfigStructMapper = pushConfigStructMapper;
    }

    @Override
    public void createTask(String dedeuserid, String sessdata, String biliJct, TaskConfigDTO taskConfigDTO) {

        // 保存用户信息
        //boolean exists = bilibiliUserMapper.exists(Wrappers.lambdaQuery(BiliTaskUserDO.class).eq(BiliTaskUserDO::getDedeuserid, dedeuserid));
        //if (!exists) {
        //    BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct);
        //    BiliUser userSpaceInfo = delegate.getUserDetails();
        //    JSONObject medalWallResp = delegate.getMedalWall();
        //    BiliTaskUserDO biliUserDO = BiliUserParser.parseUser(userSpaceInfo, medalWallResp);
        //    bilibiliUserMapper.insert(biliUserDO);
        //}

        TaskConfigDO taskConfigDO = taskConfigStructMapper.toDO(taskConfigDTO);
        taskConfigDO.setDedeuserid(dedeuserid);
        taskConfigDO.setSessdata(sessdata);
        taskConfigDO.setBiliJct(biliJct);
        // 保存用户任务配置信息
        if (taskConfigMapper.exists(Wrappers.lambdaQuery(TaskConfigDO.class).eq(TaskConfigDO::getDedeuserid, dedeuserid))) {
            taskConfigMapper.updateById(taskConfigDO);
        } else {
            taskConfigMapper.insert(taskConfigDO);
        }

        // 保存推送配置信息
        PushConfigDTO pushConfigDTO = taskConfigDTO.getPushConfig();
        PushConfigDO pushConfigDO = pushConfigStructMapper.toDO(pushConfigDTO);
        pushConfigDO.setDedeuserid(dedeuserid);

        if (pushConfigMapper.exists(Wrappers.lambdaQuery(PushConfigDO.class).eq(PushConfigDO::getDedeuserid, dedeuserid))) {
            pushConfigMapper.updateById(pushConfigDO);
        } else {
            pushConfigMapper.insert(pushConfigDO);
        }

    }

    @Override
    public boolean isExist(String dedeuserid) {
        return taskConfigMapper.exists(Wrappers.lambdaQuery(TaskConfigDO.class).eq(TaskConfigDO::getDedeuserid, dedeuserid));
    }

    @Override
    public void removeTask(String dedeuserid) {
        taskConfigMapper.deleteById(dedeuserid);
        bilibiliUserMapper.deleteById(dedeuserid);
    }

    @Override
    public TaskConfigVO getTask(String dedeuserId, String sessdata, String biliJct) {
        TaskConfigDO taskConfigDO = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfigDO.class)
                .eq(TaskConfigDO::getDedeuserid, dedeuserId)
                .eq(TaskConfigDO::getSessdata, sessdata)
                .eq(TaskConfigDO::getBiliJct, biliJct));
        return taskConfigStructMapper.toVO(taskConfigDO);
    }

    @Override
    public TaskConfigVO getTask(String dedeuserId) {
        TaskConfigDO taskConfigDO = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfigDO.class)
                .eq(TaskConfigDO::getDedeuserid, dedeuserId));
        return taskConfigStructMapper.toVO(taskConfigDO);
    }

    @Override
    public void updateCookie(String dedeuserid, String sessdata, String biliJct) {
        TaskConfigDO taskConfigDO = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfigDO.class)
                .eq(TaskConfigDO::getDedeuserid, dedeuserid));
        if (taskConfigDO != null) {
            taskConfigDO.setSessdata(sessdata);
            taskConfigDO.setBiliJct(biliJct);
            taskConfigMapper.updateById(taskConfigDO);
        }
    }

    @Override
    public List<TaskConfigDO> getTask(List<String> dedeuseridList) {
        return taskConfigMapper.selectList(Wrappers.lambdaQuery(TaskConfigDO.class)
                .in(TaskConfigDO::getDedeuserid, dedeuseridList));
    }
}
