package io.cruii.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    private String dedeuserid;

    private String tgBotToken;

    private String tgBotChatId;

    private String scKey;

    private String corpId;

    private String corpSecret;

    private String agentId;

    private String mediaId;

    private String barkDeviceKey;
}
