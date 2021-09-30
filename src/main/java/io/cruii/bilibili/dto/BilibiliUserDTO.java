package io.cruii.bilibili.dto;

import io.cruii.bilibili.entity.Medal;
import lombok.Data;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/9/30
 */
@Data
public class BilibiliUserDTO {
    private String username;

    private String avatar;

    private Integer level;

    private String dedeuserid;

    private String coins;

    private Integer currentExp;

    private Integer diffExp;

    private Integer upgradeDays;

    private List<Medal> medals;

    private Integer vipStatus;

    private Boolean isLogin;
}
