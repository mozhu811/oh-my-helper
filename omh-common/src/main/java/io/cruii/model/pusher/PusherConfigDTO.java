package io.cruii.model.pusher;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "channel", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BarkPusherConfig.class, name = "bark"),
        @JsonSubTypes.Type(value = ServerChanPusherConfig.class, name = "serverChan"),
        @JsonSubTypes.Type(value = TelegramBotPusherConfig.class, name = "telegram"),
})
public interface PusherConfigDTO {
}
