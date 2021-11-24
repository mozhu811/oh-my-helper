package io.cruii.bilibili.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author cruii
 * Created on 2021/11/22
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tencent")
public class TencentCloudConfig {

    private String secretId;

    private String secretKey;

    private String cosRegion;

    private String bucketName;

    private String folder;
}
