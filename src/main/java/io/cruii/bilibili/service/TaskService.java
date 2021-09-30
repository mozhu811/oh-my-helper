package io.cruii.bilibili.service;

import io.cruii.bilibili.dto.TaskConfigDTO;

/**
 * @author cruii
 * Created on 2021/6/6
 */
public interface TaskService {

    /**
     * 创建新任务
     *
     * @param taskConfig 任务配置
     * @return 配置是否有效
     */
    boolean createTask(TaskConfigDTO taskConfig);

    /**
     * 是否存在任务配置
     *
     * @param dedeuserid 用户ID
     * @return 是否存在
     */
    boolean isExist(String dedeuserid);
}
