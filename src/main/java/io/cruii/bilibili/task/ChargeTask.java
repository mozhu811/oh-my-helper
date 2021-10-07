package io.cruii.bilibili.task;

import cn.hutool.json.JSONObject;
import io.cruii.bilibili.context.BilibiliUserContext;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;

/**
 * @author cruii
 * Created on 2021/9/18
 */
@Log4j2
public class ChargeTask extends AbstractTask {
    private static final String AUTHOR_MID = "287969457";

    public ChargeTask(TaskConfig config) {
        super(config);
    }

    @Override
    public void run() {
        if (Boolean.FALSE.equals(config.getAutoCharge())) {
            log.info("[{}]未启用自动充电功能 ❌", config.getDedeuserid());
            return;
        }

        BilibiliUser user = BilibiliUserContext.get();

        Integer vipType = user.getVipType();
        if (vipType == 0 || vipType == 1) {
            log.info("账号非年费大会员，停止执行充电任务 ❌");
            return;
        }

        JSONObject resp = delegate.getChargeInfo();
        int couponBalance;
        if (resp.getInt(CODE) == 0) {
            couponBalance = resp.getByPath("data.bp_wallet.coupon_balance", Integer.class);
        } else {
            log.error("获取充电信息失败：{} ❌", resp.getStr(MESSAGE));
            return;
        }

        if (couponBalance < 2) {
            log.info("B币券余额不足，停止执行充电任务 ❌");
            return;
        }
        String targetId = config.getAutoChargeTarget();
        if (Objects.equals(targetId, "0")) {
            targetId = config.getDedeuserid();
        }

        BilibiliUser targetUser = delegate.getUser(targetId);
        if (targetUser == null) {
            log.info("充电对象不存在，将为作者[{}]进行充电", AUTHOR_MID);
            targetId = AUTHOR_MID;
        }

        JSONObject chargeResp = delegate.doCharge(couponBalance, targetId);
        if (chargeResp.getInt(CODE) == 0) {
            Integer status = chargeResp.getByPath("data.status", Integer.class);
            if (status == 4) {
                log.info("充电成功，本次消费[{}]个B币券 ✔️", couponBalance);
                String orderNo = chargeResp.getByPath("data.order_no", String.class);
                JSONObject commentResp = delegate.doChargeComment(orderNo);
                if (commentResp.getInt(CODE) != 0) {
                    log.error("充电留言失败：{} ❌", commentResp.getStr(MESSAGE));
                }
            }
        }
    }

    @Override
    public String getName() {
        return "充电";
    }
}
