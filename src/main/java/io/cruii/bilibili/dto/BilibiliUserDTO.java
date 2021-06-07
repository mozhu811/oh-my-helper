package io.cruii.bilibili.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BilibiliUserDTO implements Serializable {
    private static final long serialVersionUID = -6073754165745971101L;

    private String dedeuserid;

    private String username;

    private String avatar;

    private Double coins;

    private Integer level;

    private Integer currentExp;

    private Integer nextExp;

    private Integer vipType;

    private String key;
}
