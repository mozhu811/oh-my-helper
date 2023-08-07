package io.cruii.model.pusher;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TelegramBotPusherConfigVO extends PusherConfigVO {
    private String tgBotToken;

    private String tgChatId;
}
