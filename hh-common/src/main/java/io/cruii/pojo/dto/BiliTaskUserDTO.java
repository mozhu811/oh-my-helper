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

    private Integer nextExp;

    private Integer upgradeDays;

    private Integer vipStatus;

    private Integer vipType;

    private String vipLabelTheme;

    private LocalDateTime lastRunTime;

    private Boolean isLogin;
}
