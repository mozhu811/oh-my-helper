package io.cruii.bilibili.task;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/9/17
 */
@Log4j2
public class DonateGiftTask extends AbstractTask {
    private static final String AUTHOR_MID = "287969457";
    private static final String AUTHOR_ROOM_ID = "11526309";

    private String userId;

    private String roomId;

    private final TaskConfig config;

    public DonateGiftTask(BilibiliDelegate delegate) {
        super(delegate);
        this.config = delegate.getConfig();
    }

    @Override
    public void run() {
        if (!Boolean.TRUE.equals(config.getDonateGift())) {
            log.info("未启用赠送即将过期礼物 ❌");
            return;
        }

        // 初始化主播id和直播间id
        init();

        JSONObject resp = delegate.listGifts();
        if (resp.getInt(CODE) == 0) {
            String gifts = resp.getByPath("data.list", String.class);
            if (gifts == null) {
                log.info("背包无礼物，停止执行此任务 ❌");
                return;
            }
            List<JSONObject> expireGifts = new JSONArray(gifts).stream().map(JSONUtil::parseObj)
                    .filter(gift -> {
                        Long expireAt = gift.getLong("expire_at");
                        long now = System.currentTimeMillis() / 1000;
                        // 过滤3天内过期的礼物
                        return expireAt != 0 && (expireAt - now) < 60 * 60 * 24 * 3;
                    }).collect(Collectors.toList());

            if (expireGifts.isEmpty()) {
                log.info("背包中没有即将过期礼物 ✔️");
                return;
            }

            expireGifts.forEach(gift -> {
                JSONObject respDonate = delegate.donateGift(userId, roomId,
                        gift.getStr("bag_id"),
                        gift.getStr("gift_id"),
                        gift.getInt("gift_num"));
                if (respDonate.getInt(CODE) == 0) {
                    String giftName = respDonate.getByPath("data.gift_name", String.class);
                    String giftNum = respDonate.getByPath("data.gift_num", String.class);
                    log.info("给直播间[{}]赠送了[{}]个[{}] ✔️", roomId, giftNum, giftName);
                } else {
                    log.error("给直播间[{}]赠送礼物失败：{} ❌", roomId, respDonate.getStr(MESSAGE));
                }
            });

        } else {
            log.error("无法获取礼物背包 ❌");
        }
    }

    /**
     * 获取直播间ID
     */
    private void init() {
        userId = config.getDonateGiftTarget();
        if ("0".equals(userId)) {
            userId = AUTHOR_MID;
        }
        JSONObject resp = delegate.getLiveRoomInfo(userId);
        if (resp.getInt(CODE) == 0) {
            roomId = resp.getByPath("data.roomid", String.class);
            return;
        }
        log.error("获取直播间信息失败，将为作者直播间[{}]送出礼物", AUTHOR_ROOM_ID);
        roomId = AUTHOR_ROOM_ID;
    }

    @Override
    public String getName() {
        return "赠送直播间即将过期礼物";
    }
}
