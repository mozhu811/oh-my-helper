package io.cruii.bilibili.service;

import io.cruii.bilibili.dto.TaskConfigDTO;
import io.cruii.bilibili.entity.TaskConfig;

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
    boolean createTask(TaskConfigDTO taskConfig) throws InterruptedException;

    /**
     * 是否存在任务配置
     *
     * @param dedeuserid 用户ID
     * @return 是否存在
     */
    boolean isExist(String dedeuserid);

    void removeTask(String dedeuserid);

    TaskConfig getTask(String dedeuserId, String sessdata, String biliJct);

    void updateCookie(String dedeuserid, String sessdata, String biliJct);
}
