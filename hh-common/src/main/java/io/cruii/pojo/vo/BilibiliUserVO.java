package io.cruii.pojo.vo;

import cn.hutool.json.JSONArray;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/6/10
 */
@Data
@Accessors(chain = true)
public class BilibiliUserVO implements Serializable {
    private static final long serialVersionUID = -4960479906705315906L;

    private String dedeuserid;

    private String username;

    private Integer level;

    private String avatar;

    private String coins;

    private Integer currentExp;

    private Integer diffExp;

    private Integer upgradeDays;

    private JSONArray medals;

    private Integer vipStatus;

    private Boolean isLogin;

    private Long configId;
}
