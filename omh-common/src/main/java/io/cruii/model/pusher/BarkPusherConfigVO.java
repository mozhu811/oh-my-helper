package io.cruii.model.pusher;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BarkPusherConfigVO extends PusherConfigVO {

    private String barkDeviceKey;

}
