package io.cruii.bilibili.vo;

import cn.hutool.core.bean.BeanUtil;
import io.cruii.bilibili.dto.CreateContainerDTO;
import io.cruii.bilibili.entity.ContainerConfig;
import lombok.Data;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/6/8
 */
@Data
public class CreateContainerVO implements Serializable {
    private static final long serialVersionUID = 3568897779536110128L;

    private String containerName;

    private String description;

    private ContainerConfig config;

    public CreateContainerDTO toDto() {
        CreateContainerDTO createContainerDTO = new CreateContainerDTO();
        BeanUtil.copyProperties(this, createContainerDTO);
        return createContainerDTO;
    }
}
