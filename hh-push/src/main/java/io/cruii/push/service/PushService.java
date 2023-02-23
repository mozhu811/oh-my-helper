package io.cruii.push.service;

import io.cruii.pojo.dto.PushMessageDTO;

/**
 * @author cruii
 * Created on 2022/4/6
 */
public interface PushService {
    boolean push(PushMessageDTO messageDTO);
}
