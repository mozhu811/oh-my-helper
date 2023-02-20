package io.cruii.task;

import cn.hutool.json.JSONObject;
import io.cruii.component.BilibiliDelegate;
import io.cruii.context.BilibiliUserContext;
import io.cruii.model.BiliUser;
import io.cruii.model.SpaceAccInfo;
import io.cruii.pojo.entity.TaskConfigDO;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;

/**
 * @author cruii
 * Created on 2021/9/18
 */
@Log4j2
public class ChargeTask extends AbstractTask {
    private static final String AUTHOR_MID = "287969457";

    private final TaskConfigDO config;

    public ChargeTask(BilibiliDelegate delegate) {
        super(delegate);
        this.config = delegate.getConfig();
    }

    @Override
    public void run() throws Exception {
        BiliUser biliUser = BilibiliUserContext.get();

        Integer vipType = biliUser.getVip().getType();
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
            log.info("充电对象设置为 0，将为作者[{}]进行充电，感谢您对本项目的支持", AUTHOR_MID);
            targetId = AUTHOR_MID;
        }

        SpaceAccInfo spaceAccInfo = delegate.getSpaceAccInfo(targetId);
        if (spaceAccInfo == null) {
            log.info("充电对象未找到，将为作者[{}]进行充电", AUTHOR_MID);
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
