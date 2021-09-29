package io.cruii.bilibili.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/9/27
 */
@Data
public class Medal implements Serializable {
    private static final long serialVersionUID = -3237387772474749574L;

    private String name;

    private Integer level;

    private Integer colorStart;

    private Integer colorEnd;

    private Integer colorBorder;
}
