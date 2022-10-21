package io.cruii.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2022/4/6
 */
@Data
@TableName("push_config")
public class PushConfig implements Serializable {
    private static final long serialVersionUID = -4986884028136127740L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String dedeuserid;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String tgBotToken;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String tgBotChatId;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String scKey;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String corpId;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String corpSecret;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String agentId;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String mediaId;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String barkDeviceKey;
}
