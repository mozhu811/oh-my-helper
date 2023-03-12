package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class SendGiftResponse {
    private String tid;
    private Integer uid;
    private String uname;
    private String face;
    private Integer guardLevel;
    private Integer ruid;
    private Integer rcost;
    private Integer giftId;
    private Integer giftType;
    private String giftName;
    private Integer giftNum;
    private String giftAction;
    private Integer giftPrice;
    private String coinType;
    private Integer totalCoin;
    private Integer payCoin;
    private String metadata;
    private String fulltext;
    private String rnd;
    private String tagImage;
    private Integer effectBlock;
    private Extra extra;
    private Integer blowSwitch;
    private String sendTips;
    private Integer discountId;
    private GiftEffect giftEffect;
    private Object sendMaster;
    private Integer critProb;
    private Integer comboStayTime;
    private Integer comboTotalCoin;
    private Integer demarcation;
    private Integer magnification;
    private Integer comboResourcesId;
    private Integer isSpecialBatch;
    private Integer sendGiftCountdown;
    private Integer bpCentBalance;
    private Integer price;
    private Integer leftNum;
    private Integer needNum;
    private Integer availableNum;

    @NoArgsConstructor
    @Data
    public static class Extra {
        private Object wallet;
        private GiftBag giftBag;
        private List<?> topList;
        private Object follow;
        private Object medal;
        private Object title;
        private Pk pk;
        private String fulltext;
        private Event event;
        private Object capsule;
        private String lotteryId;

        @NoArgsConstructor
        @Data
        public static class GiftBag {
            private Integer bagId;
            private Integer giftNum;
        }

        @NoArgsConstructor
        @Data
        public static class Pk {
            private String pkGiftTips;
            private Integer critProb;
        }

        @NoArgsConstructor
        @Data
        public static class Event {
            private Integer eventScore;
            private Integer eventRedbagNum;
        }
    }

    @NoArgsConstructor
    @Data
    public static class GiftEffect {
        private Integer superX;
        private Integer comboTimeout;
        private Integer superGiftNum;
        private Integer superBatchGiftNum;
        private String batchComboId;
        private List<?> broadcastMsgList;
        private List<?> smallTvList;
        private Object beatStorm;
        private String comboId;
        private Boolean smallTVCountFlag;
    }
}
