package io.cruii.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@Configuration
@ConfigurationProperties(prefix = "netty.websocket")
@Data
public class NettyConfiguration {
    private Integer port;

    private String host;

    private Integer maxFrameSize;

    private String path;
}
