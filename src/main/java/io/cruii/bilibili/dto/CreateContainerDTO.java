package io.cruii.bilibili.dto;

import io.cruii.bilibili.entity.ContainerConfig;
import lombok.Data;

/**
 * @author cruii
 * Created on 2021/6/9
 */
@Data
public class CreateContainerDTO {

    private String containerName;

    private String description;

    private String dedeuserid;

    private String sessdata;

    private String biliJct;

    private ContainerConfig config;
}
