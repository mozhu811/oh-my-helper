package io.cruii.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author cruii
 * Created on 2021/9/30
 */
@Data
public class BilibiliUserDTO {

    private String dedeuserid;

    private String username;

    private Integer level;

    private String coins;

    private Integer currentExp;

    private Integer diffExp;

    private Integer upgradeDays;

    private String medals;

    private Integer vipStatus;

    private LocalDateTime lastRunTime;

    private Boolean isLogin;
}
