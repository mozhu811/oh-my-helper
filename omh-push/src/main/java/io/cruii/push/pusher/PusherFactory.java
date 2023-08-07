package io.cruii.push.pusher;

import cn.hutool.json.JSONUtil;
import io.cruii.constant.PushChannel;
import io.cruii.model.pusher.ServerChanPusherConfig;
import io.cruii.model.pusher.TelegramBotPusherConfig;
import io.cruii.push.pusher.impl.ServerChanPusher;
import io.cruii.push.pusher.impl.TelegramBotPusher;

public class PusherFactory {
    private PusherFactory() {
    }

    public static Pusher create(Integer channel, Object config) {
        PushChannel pushChannel = PushChannel.valueOf(channel);

        switch (pushChannel) {
            case SERVER_CHAN:
                ServerChanPusherConfig pusherConfig = JSONUtil.parseObj(config).toBean(ServerChanPusherConfig.class);
                return new ServerChanPusher(pusherConfig);
            case TELEGRAM:
                return new TelegramBotPusher(((TelegramBotPusherConfig) config));
            //case QY_WECHAT:
            //    new QyWechatPusher(config.getCorpId(), config.getCorpSecret(), config.getAgentId(), config.getMediaId());
            //    break;
            //case BARK:
            //    new BarkPusher(config.getBarkDeviceKey());
            //    break;
            //case FEISHU:
            //    new FeiShuCustomBotPusher(config.getFeiShuCustomBot());
            //    break;
            default:
                throw new IllegalArgumentException("不支持或未配置推送渠道");
        }
    }
}
