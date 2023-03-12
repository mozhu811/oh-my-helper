package io.cruii.service;


import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.pojo.entity.TaskConfigDO;
import io.cruii.pojo.vo.TaskConfigVO;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/6/6
 */
public interface TaskConfigService {

    /**
     * 是否存在任务配置
     *
     * @param dedeuserid 用户ID
     * @return 是否存在
     */
    boolean isExist(String dedeuserid);

    void removeTask(String dedeuserid);

    TaskConfigVO getTask(String dedeuserId, String sessdata, String biliJct);

    TaskConfigVO getTask(String dedeuserId);

    void updateCookie(String dedeuserid, String sessdata, String biliJct);

    List<TaskConfigDO> getTask(List<String> dedeuseridList);

    void createTask(String dedeuserid, String sessdata, String biliJct, TaskConfigDTO taskConfigDTO);
}
