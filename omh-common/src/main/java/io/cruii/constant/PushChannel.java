package io.cruii.constant;

public enum PushChannel {
    /**
     * 定义推送渠道
     * -1 - None
     * 0 - ServerChan
     * 1 - Telegram
     * 2 - QyWechat
     * 3 - Bark
     * 4 - Feishu
     */
    NONE(-1),
    SERVER_CHAN(0),
    TELEGRAM(1),
    QY_WECHAT(2),
    BARK(3),
    FEISHU(4);

    private final int value;

    PushChannel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PushChannel valueOf(int value) {
        for (PushChannel pushChannel : PushChannel.values()) {
            if (pushChannel.getValue() == value) {
                return pushChannel;
            }
        }
        return NONE;
    }
}
