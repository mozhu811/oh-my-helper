package io.cruii.bilibili.task;

import cn.hutool.json.JSONObject;
import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;

/**
 * @author cruii
 * Created on 2021/9/17
 */
@Log4j2
public class Silver2CoinTask extends AbstractTask {
    private static final int THRESHOLD = 700;

    public Silver2CoinTask(TaskConfig config) {
        super(config);
    }

    @Override
    public void run() {
        if (checkWallet()) {
            doExchange();
            log.info("当前银瓜子余额为：{}", getNumOfSilver());
        } else {
            log.info("银瓜子余额为[{}]，不足[{}]，无法执行兑换", getNumOfSilver(), THRESHOLD);
        }
    }

    /**
     * 检查钱包余额
     *
     * @return 是否大于阈值
     */
    private boolean checkWallet() {
        int current = getNumOfSilver();
        return current >= THRESHOLD;
    }

    /**
     * 获取银瓜子余额
     *
     * @return 银瓜子余额
     */
    private int getNumOfSilver() {
        JSONObject resp = delegate.getLiveWallet();
        if (resp == null || resp.getObj("data") == null) {
            return 0;
        }
        JSONObject wallet = resp.getJSONObject("data");
        return wallet.getInt("silver");
    }

    /**
     * 执行银瓜子兑换硬币
     */
    private void doExchange() {
        JSONObject resp = delegate.silver2Coin();
        if (resp.getInt(CODE) == 0) {
            log.info("兑换硬币成功 ✔️");
        } else {
            log.error("兑换失败：{} ❌", resp.getStr(MESSAGE));
        }
    }

    @Override
    public String getName() {
        return "银瓜子兑换硬币";
    }
}
