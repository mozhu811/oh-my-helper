package io.cruii.bilibili.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Configuration
@ConfigurationProperties(prefix = "tencent")
@Data
public class TencentApiConfig {
    private String secretId;

    private String secretKey;

    private String clsEndpoint;

    private String clsTopicId;

    private String scfEndpoint;

    private String region;
}
