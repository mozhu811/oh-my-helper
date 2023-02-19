package io.cruii.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2022/4/7
 */
@Data
public class PushConfigDTO implements Serializable {

    private static final long serialVersionUID = 8628766559103661462L;

    private String tgBotToken;

    private String tgBotChatId;

    private String scKey;

    private String corpId;

    private String corpSecret;

    private String agentId;

    private String mediaId;

    private String barkDeviceKey;
}
