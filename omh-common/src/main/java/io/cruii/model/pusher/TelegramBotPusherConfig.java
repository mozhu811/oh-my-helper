package io.cruii.model.pusher;

import io.cruii.annatation.ChannelConstraint;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class TelegramBotPusherConfig implements PusherConfigDTO {
    @NotEmpty(message = "tgBotToken不能为空")
    private String tgBotToken;

    @NotEmpty(message = "tgChatId不能为空")
    private String tgChatId;

    @NotNull(message = "channel不能为空")
    @ChannelConstraint
    private String channel;
}
