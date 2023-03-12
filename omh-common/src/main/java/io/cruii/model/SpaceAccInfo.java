package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author cruii
 * Created on 2023/2/17
 */
@NoArgsConstructor
@Data
public class SpaceAccInfo {
    private Integer mid;
    private String name;
    private String sex;
    private String face;
    private Integer faceNft;
    private Integer faceNftType;
    private String sign;
    private Integer rank;
    private Integer level;
    private Integer jointime;
    private Integer moral;
    private Integer silence;
    private Double coins;
    private Boolean fansBadge;
    private FansMedal fansMedal;
    private Official official;
    private Vip vip;
    private Pendant pendant;
    private Nameplate nameplate;
    private UserHonourInfo userHonourInfo;
    private Boolean isFollowed;
    private String topPhoto;
    private Theme theme;
    private SysNotice sysNotice;
    private LiveRoom liveRoom;
    private String birthday;
    private School school;
    private Profession profession;
    private Object tags;
    private Series series;
    private Integer isSeniorMember;
    private Object mcnInfo;
    private Integer gaiaResType;
    private Object gaiaData;
    private Boolean isRisk;
    private Elec elec;
    private Contract contract;

    @NoArgsConstructor
    @Data
    public static class FansMedal {
        private Boolean show;
        private Boolean wear;
        private Medal medal;

        @NoArgsConstructor
        @Data
        public static class Medal {
            private Integer uid;
            private Integer targetId;
            private Integer medalId;
            private Integer level;
            private String medalName;
            private Integer medalColor;
            private Integer intimacy;
            private Integer nextIntimacy;
            private Integer dayLimit;
            private Integer medalColorStart;
            private Integer medalColorEnd;
            private Integer medalColorBorder;
            private Integer isLighted;
            private Integer lightStatus;
            private Integer wearingStatus;
            private Integer score;
        }
    }

    @NoArgsConstructor
    @Data
    public static class Official {
        private Integer role;
        private String title;
        private String desc;
        private Integer type;
    }

    @NoArgsConstructor
    @Data
    public static class Vip {
        private Integer type;
        private Integer status;
        private Long dueDate;
        private Integer vipPayType;
        private Integer themeType;
        private Label label;
        private Integer avatarSubscript;
        private String nicknameColor;
        private Integer role;
        private String avatarSubscriptUrl;
        private Integer tvVipStatus;
        private Integer tvVipPayType;

        @NoArgsConstructor
        @Data
        public static class Label {
            private String path;
            private String text;
            private String labelTheme;
            private String textColor;
            private Integer bgStyle;
            private String bgColor;
            private String borderColor;
            private Boolean useImgLabel;
            private String imgLabelUriHans;
            private String imgLabelUriHant;
            private String imgLabelUriHansStatic;
            private String imgLabelUriHantStatic;
        }
    }

    @NoArgsConstructor
    @Data
    public static class Pendant {
        private Integer pid;
        private String name;
        private String image;
        private Integer expire;
        private String imageEnhance;
        private String imageEnhanceFrame;
    }

    @NoArgsConstructor
    @Data
    public static class Nameplate {
        private Integer nid;
        private String name;
        private String image;
        private String imageSmall;
        private String level;
        private String condition;
    }

    @NoArgsConstructor
    @Data
    public static class UserHonourInfo {
        private Integer mid;
        private String color;

        private List<Object> tags;
    }

    @NoArgsConstructor
    @Data
    public static class Theme {
        // 作用不明，此处为了hutool的反序列化而设置一个占位对象
        // 如果没有set方法，hutool不会认为这是一个bean，导致无法反序列化
        private Object unknown;
    }

    @NoArgsConstructor
    @Data
    public static class SysNotice {
        private Integer id;

        private String content;

        private String url;

        private Integer noticeType;

        private String icon;

        private String textColor;

        private String bigColor;
    }

    @NoArgsConstructor
    @Data
    public static class LiveRoom {
        private Integer roomStatus;
        private Integer liveStatus;
        private String url;
        private String title;
        private String cover;
        private Integer roomid;
        private Integer roundStatus;
        private Integer broadcastType;
        private WatchedShow watchedShow;

        @NoArgsConstructor
        @Data
        public static class WatchedShow {
            private Boolean switchX;
            private Integer num;
            private String textSmall;
            private String textLarge;
            private String icon;
            private String iconLocation;
            private String iconWeb;
        }
    }

    @NoArgsConstructor
    @Data
    public static class School {
        private String name;
    }

    @NoArgsConstructor
    @Data
    public static class Profession {
        private String name;
        private String department;
        private String title;
        private Integer isShow;
    }

    @NoArgsConstructor
    @Data
    public static class Series {
        private Integer userUpgradeStatus;
        private Boolean showUpgradeWindow;
    }

    @NoArgsConstructor
    @Data
    public static class Elec {
        private ShowInfo showInfo;

        @NoArgsConstructor
        @Data
        public static class ShowInfo {
            private Boolean show;
            private Integer state;
            private String title;
            private String icon;
            private String jumpUrl;
        }
    }

    @NoArgsConstructor
    @Data
    public static class Contract {
        private Boolean isDisplay;
        private Boolean isFollowDisplay;
    }
}
