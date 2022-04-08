package io.cruii.pojo.vo;

import lombok.Data;

/**
 * @author cruii
 * Created on 2022/4/7
 */
@Data
public class PushConfigVO {
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
