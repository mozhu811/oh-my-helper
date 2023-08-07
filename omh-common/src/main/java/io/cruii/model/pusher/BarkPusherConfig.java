package io.cruii.model.pusher;

import io.cruii.annatation.ChannelConstraint;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BarkPusherConfig implements PusherConfigDTO {

    @NotNull(message = "BarkDeviceKey不能为空")
    private final String barkDeviceKey;

    @NotNull(message = "channel不能为空")
    @ChannelConstraint
    private final String channel;
}
