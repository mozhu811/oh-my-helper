package io.cruii.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2022/4/7
 */
@Data
public class PushConfigVO implements Serializable {

    private static final long serialVersionUID = 8628766559103661462L;

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
