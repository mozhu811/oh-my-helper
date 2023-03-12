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
public class MedalWall {
    private List<Medal> list;
    private Integer count;
    private Integer closeSpaceMedal;
    private Integer onlyShowWearing;
    private String name;
    private String icon;
    private Integer uid;
    private Integer level;

    @NoArgsConstructor
    @Data
    public static class Medal {
        private MedalInfo medalInfo;
        private String targetName;
        private String targetIcon;
        private String link;
        private Integer liveStatus;
        private Integer official;

        @NoArgsConstructor
        @Data
        public static class MedalInfo {
            private Integer targetId;
            private Integer level;
            private String medalName;
            private Integer medalColorStart;
            private Integer medalColorEnd;
            private Integer medalColorBorder;
            private Integer guardLevel;
            private Integer wearingStatus;
            private Integer medalId;
            private Integer intimacy;
            private Integer nextIntimacy;
            private Integer todayFeed;
            private Integer dayLimit;
            private String guardIcon;
            private String honorIcon;
        }
    }
}
