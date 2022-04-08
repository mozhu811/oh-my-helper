package io.cruii.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @author cruii
 * Created on 2021/9/14
 */
@Data
@TableName("bilibili_user")
public class BilibiliUser implements Serializable{

    private static final long serialVersionUID = -1589103932581149105L;

    private Long id;

    @TableId(type = IdType.INPUT)
    private String dedeuserid;

    private String username;

    private String coins;

    private Integer level;

    private Integer currentExp;

    private Integer nextExp;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer upgradeDays;

    private String medals;

    private Integer vipType;

    private Integer vipStatus;

    private Boolean isLogin;

    private LocalDateTime lastRunTime;

    private LocalDateTime createTime;
}
