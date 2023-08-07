package io.cruii.model.pusher;

import io.cruii.annatation.ChannelConstraint;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ServerChanPusherConfig implements PusherConfigDTO {
    @NotNull(message = "scKey不能为空")
    private final String scKey;

    @NotNull(message = "channel不能为空")
    @ChannelConstraint
    private final String channel;
}
