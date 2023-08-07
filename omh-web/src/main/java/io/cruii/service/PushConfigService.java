package io.cruii.service;

import io.cruii.model.pusher.PusherConfigDTO;
import io.cruii.model.pusher.PusherConfigVO;

public interface PushConfigService {
    PusherConfigVO saveOrUpdate(String userId, PusherConfigDTO pusherConfig);

    PusherConfigVO get(String dedeuserid, String sessdata, String biliJct);
}
