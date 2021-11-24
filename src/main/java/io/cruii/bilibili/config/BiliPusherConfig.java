package io.cruii.bilibili.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author cruii
 * Created on 2021/11/24
 */
@Configuration
public class BiliPusherConfig {
    @Getter
    @Value("${task.push.dedeuserid:22056408}")
    private String dedeuserid;
}
