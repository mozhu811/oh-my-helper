package io.cruii.push.service;

import io.cruii.pojo.dto.PushConfigDTO;

/**
 * @author cruii
 * Created on 2022/4/6
 */
public interface PushService {
    boolean push(String dedeuserid, String content);

    void save(PushConfigDTO pushConfigDTO);
}
