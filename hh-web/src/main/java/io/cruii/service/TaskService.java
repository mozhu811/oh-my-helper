package io.cruii.service;


import io.cruii.pojo.dto.PushConfigDTO;
import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.pojo.po.TaskConfig;

/**
 * @author cruii
 * Created on 2021/6/6
 */
public interface TaskService {

    /**
     * 创建新任务
     *
     * @param taskConfigDTO 任务配置
     * @param pushConfigDTO 推送配置
     * @return 配置是否有效
     */
    TaskConfigDTO createTask(TaskConfigDTO taskConfigDTO, PushConfigDTO pushConfigDTO);

    /**
     * 是否存在任务配置
     *
     * @param dedeuserid 用户ID
     * @return 是否存在
     */
    boolean isExist(String dedeuserid);

    void removeTask(String dedeuserid);

    TaskConfig getTask(String dedeuserId, String sessdata, String biliJct);

    TaskConfig getTask(String dedeuserId);

    void updateCookie(String dedeuserid, String sessdata, String biliJct);
}
