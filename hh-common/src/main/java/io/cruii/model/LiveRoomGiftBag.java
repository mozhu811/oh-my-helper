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
public class LiveRoomGiftBag {
    private List<Gift> gifts;
    private Integer time;

    @NoArgsConstructor
    @Data
    public static class Gift {
        private Integer bagId;
        private Integer giftId;
        private String giftName;
        private Integer giftNum;
        private Integer giftType;
        private Integer expireAt;
        private String cornerMark;
        private String cornerColor;
        private List<Gift.CountMap> countMap;
        private Integer bindRoomid;
        private String bindRoomText;
        private Integer type;
        private String cardImage;
        private String cardGif;
        private Integer cardId;
        private Integer cardRecordId;
        private Boolean isShowSend;

        @NoArgsConstructor
        @Data
        public static class CountMap {
            private Integer num;
            private String text;
        }
    }
}
