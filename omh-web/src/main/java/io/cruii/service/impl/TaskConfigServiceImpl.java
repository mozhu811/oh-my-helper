package io.cruii.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.component.BilibiliDelegate;
import io.cruii.component.TaskConfigStructMapper;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.pojo.entity.TaskConfigDO;
import io.cruii.pojo.vo.TaskConfigVO;
import io.cruii.service.BilibiliUserService;
import io.cruii.service.TaskConfigService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Service
@Log4j2
@Transactional(rollbackFor = Exception.class)
public class TaskConfigServiceImpl implements TaskConfigService {

    private final BilibiliUserService bilibiliUserService;

    private final TaskConfigMapper taskConfigMapper;

    private final TaskConfigStructMapper taskConfigStructMapper;


    public TaskConfigServiceImpl(BilibiliUserService bilibiliUserService,
                                 TaskConfigMapper taskConfigMapper,
                                 TaskConfigStructMapper taskConfigStructMapper) {
        this.bilibiliUserService = bilibiliUserService;
        this.taskConfigMapper = taskConfigMapper;
        this.taskConfigStructMapper = taskConfigStructMapper;
    }

    @Override
    public TaskConfigVO saveOrUpdate(String dedeuserid, String sessdata, String biliJct, TaskConfigDTO taskConfigDTO) {
        TaskConfigDO taskConfigDO = taskConfigStructMapper.toDO(taskConfigDTO);
        taskConfigDO.setDedeuserid(dedeuserid);
        taskConfigDO.setSessdata(sessdata);
        taskConfigDO.setBiliJct(biliJct);

        Optional<TaskConfigDO> exist = Optional.ofNullable(taskConfigMapper.selectOne(
                        Wrappers.<TaskConfigDO>lambdaQuery().eq(TaskConfigDO::getDedeuserid, dedeuserid)))
                .map(e -> {
                    taskConfigDO.setId(e.getId());
                    return e;
                });
        exist.ifPresentOrElse(taskConfigMapper::updateById, () -> taskConfigMapper.insert(taskConfigDO));
        return taskConfigStructMapper.toVO(taskConfigDO);
    }


    @Override
    public boolean isExist(String dedeuserid) {
        return taskConfigMapper.exists(Wrappers.lambdaQuery(TaskConfigDO.class).eq(TaskConfigDO::getDedeuserid, dedeuserid));
    }

    @Override
    public void remove(String dedeuserid, String sessdata, String biliJct) {
        BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct, false);
        // Verify cookie
        Optional.ofNullable(delegate.getUserDetails())
                .ifPresent(userDetails -> {
                    bilibiliUserService.delete(dedeuserid);
                    taskConfigMapper.deleteById(dedeuserid);
                });
    }

    @Override
    public TaskConfigVO get(String dedeuserId, String sessdata, String biliJct) {
        sessdata = sessdata.replace(",", "%2C").replace("*", "%2A");

        String finalSessdata = sessdata;
        return Optional.ofNullable(taskConfigMapper.selectOne(
                        Wrappers.<TaskConfigDO>lambdaQuery().eq(TaskConfigDO::getDedeuserid, dedeuserId)))
                .filter(taskConfigDO -> finalSessdata.equals(taskConfigDO.getSessdata()))
                .filter(taskConfigDO -> biliJct.equals(taskConfigDO.getBiliJct()))
                .map(taskConfigStructMapper::toVO)
                .orElseThrow(() -> new RuntimeException("非法请求"));
    }


    @Override
    public void updateCookie(String dedeuserid, String sessdata, String biliJct) {
        Optional.ofNullable(taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfigDO.class)
                        .eq(TaskConfigDO::getDedeuserid, dedeuserid)))
                .ifPresent(taskConfigDO -> {
                    taskConfigDO.setSessdata(sessdata);
                    taskConfigDO.setBiliJct(biliJct);
                    taskConfigMapper.updateById(taskConfigDO);
                });
    }
}
