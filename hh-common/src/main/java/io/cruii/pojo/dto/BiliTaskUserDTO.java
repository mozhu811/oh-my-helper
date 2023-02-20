package io.cruii.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author cruii
 * Created on 2021/9/30
 */
@Data
@Accessors(chain = true)
public class BiliTaskUserDTO {

    private String dedeuserid;

    private String username;

    private Integer level;

    private String coins;

    private Integer currentExp;

    private Integer diffExp;

    private Integer upgradeDays;

    private Integer vipStatus;

    private LocalDateTime lastRunTime;

    private Boolean isLogin;
}
