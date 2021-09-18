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
    boolean createNewTask(TaskConfigDTO taskConfig);
}
