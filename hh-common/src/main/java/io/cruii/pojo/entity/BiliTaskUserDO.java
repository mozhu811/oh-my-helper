package io.cruii.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @author cruii
 * Created on 2021/9/14
 */
@Data
@Accessors(chain = true)
@TableName("bilibili_user")
public class BiliTaskUserDO implements Serializable{

    private static final long serialVersionUID = -1589103932581149105L;

    private Long id;

    @TableId(type = IdType.INPUT)
    private String dedeuserid;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String username;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String coins;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer level;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer currentExp;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer nextExp;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer upgradeDays;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String medals;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer vipType;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer vipStatus;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String sign;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isLogin;

    private LocalDateTime lastRunTime;

    private LocalDateTime createTime;
}
