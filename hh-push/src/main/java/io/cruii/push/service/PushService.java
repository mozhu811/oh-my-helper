package io.cruii.push.service;

import io.cruii.pojo.dto.PushMessageDTO;

import java.util.List;

/**
 * @author cruii
 * Created on 2022/4/6
 */
public interface PushService {
    boolean push(PushMessageDTO messageDTO);

    void notifyExpired(List<String> idList);
}
