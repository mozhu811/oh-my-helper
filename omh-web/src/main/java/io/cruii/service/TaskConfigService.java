package io.cruii.service;


import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.pojo.vo.TaskConfigVO;

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

    void remove(String dedeuserid, String sessdata, String biliJct);

    TaskConfigVO get(String dedeuserId, String sessdata, String biliJct);

    void updateCookie(String dedeuserid, String sessdata, String biliJct);

    TaskConfigVO saveOrUpdate(String dedeuserid, String sessdata, String biliJct, TaskConfigDTO taskConfigDTO);
}
