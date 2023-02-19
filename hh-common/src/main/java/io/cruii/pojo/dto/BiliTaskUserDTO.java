package io.cruii.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author cruii
 * Created on 2021/9/30
 */
@Data
public class BiliTaskUserDTO {

    private String dedeuserid;

    private String username;

    private Integer level;

    private String coins;

    private Integer currentExp;

    private Integer diffExp;

    private Integer upgradeDays;

    private String medals;

    private Integer vipStatus;

    private String sign;

    private LocalDateTime lastRunTime;

    private Boolean isLogin;
}
