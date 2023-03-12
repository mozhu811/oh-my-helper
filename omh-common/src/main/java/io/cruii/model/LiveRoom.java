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
public class LiveRoom {
    private Info info;
    private Exp exp;
    private Integer followerNum;
    private Integer roomId;
    private String medalName;
    private Integer gloryCount;
    private String pendant;
    private Integer linkGroupNum;
    private RoomNews roomNews;

    @NoArgsConstructor
    @Data
    public static class Info {
        private Integer uid;
        private String uname;
        private String face;
        private OfficialVerify officialVerify;
        private Integer gender;

        @NoArgsConstructor
        @Data
        public static class OfficialVerify {
            private Integer type;
            private String desc;
        }
    }

    @NoArgsConstructor
    @Data
    public static class Exp {
        private MasterLevel masterLevel;

        @NoArgsConstructor
        @Data
        public static class MasterLevel {
            private Integer level;
            private Integer color;
            private List<Integer> current;
            private List<Integer> next;
        }
    }

    @NoArgsConstructor
    @Data
    public static class RoomNews {
        private String content;
        private String ctime;
        private String ctimeText;
    }
}
