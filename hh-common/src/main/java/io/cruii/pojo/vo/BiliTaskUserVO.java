package io.cruii.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/6/10
 */
@Data
@Accessors(chain = true)
public class BiliTaskUserVO implements Serializable {
    private static final long serialVersionUID = -4960479906705315906L;

    private String dedeuserid;

    private String username;

    private Integer level;

    private String coins;

    private Integer currentExp;

    private Integer nextExp;

    private Integer upgradeDays;

    private String medals;

    private Integer vipStatus;

    private String vipLabelTheme;

    private String sign;

    private Boolean isLogin;
}
