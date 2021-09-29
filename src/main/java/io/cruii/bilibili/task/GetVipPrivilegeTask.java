package io.cruii.bilibili.task;

import cn.hutool.json.JSONObject;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;

/**
 * @author cruii
 * Created on 2021/9/18
 */
@Log4j2
public class GetVipPrivilegeTask extends AbstractTask {
    public GetVipPrivilegeTask(TaskConfig config) {
        super(config);
    }

    @Override
    public void run() {
        BilibiliUser user = delegate.getUser();
        Integer vipType = user.getVipType();
        if (vipType == 0 || user.getVipStatus() != 1) {
            log.info("该账号非大会员，取消执行领取大会员权益 ❌");
            return;
        }

        JSONObject rewardResp = delegate.getMangaVipReward();
        if (rewardResp.getInt(CODE) == 0) {
            int amount = rewardResp.getByPath("data.amount", Integer.class);
            log.info("成功领取{}张漫读劵 ✔️", amount);
        } else if (rewardResp.getInt(CODE) == 6) {
            log.info("本月漫读券已领取 ✔️");
        } else {
            log.info("领取漫读劵失败：{} ❌", rewardResp.getStr("msg"));
        }

        JSONObject bCoinResp = delegate.getVipReward(1);
        if (bCoinResp.getInt(CODE) == 0) {
            log.info("领取B币券成功 ✔️");
        } else if (bCoinResp.getInt(CODE) == 69801) {
            log.info("本月B币券已领取 ✔️");
        } else {
            log.info("领取B币券失败：{} ❌", bCoinResp.getStr(MESSAGE));
        }

        JSONObject vipShopReward = delegate.getVipReward(2);
        if (vipShopReward.getInt(CODE) == 0) {
            log.info("领取会员购优惠券成功 ✔️");
        } else if (vipShopReward.getInt(CODE) == 69801) {
            log.info("本月会员购优惠券已领取 ✔️");
        } else {
            log.info("领取会员购优惠券失败：{} ❌", vipShopReward.getStr(MESSAGE));
        }
    }

    @Override
    public String getName() {
        return "领取大会员权益";
    }
}
