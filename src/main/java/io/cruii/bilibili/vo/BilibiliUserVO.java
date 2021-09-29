package io.cruii.bilibili.vo;

import io.cruii.bilibili.entity.Medal;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author cruii
 * Created on 2021/6/10
 */
@Data
@Accessors(chain = true)
public class BilibiliUserVO implements Serializable {
    private static final long serialVersionUID = -4960479906705315906L;

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
