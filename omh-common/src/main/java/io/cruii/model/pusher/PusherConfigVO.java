package io.cruii.model.pusher;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "channel", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BarkPusherConfigVO.class, name = "bark"),
        @JsonSubTypes.Type(value = TelegramBotPusherConfigVO.class, name = "telegram"),
})
@Data
public abstract class PusherConfigVO {
    private Long id;

    private String channel;
}
