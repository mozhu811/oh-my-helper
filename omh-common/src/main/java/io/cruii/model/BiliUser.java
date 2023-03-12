package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class BiliUser implements BilibiliUser{
    private Integer mid;
    private String name;
    private String sex;
    private String face;
    private String sign;
    private Integer rank;
    private Integer level;
    private Integer joinTime;
    private Integer moral;
    private Integer silence;
    private Integer emailStatus;
    private Integer telStatus;
    private Integer identification;
    private Vip vip;
    private Pendant pendant;
    private Nameplate nameplate;
    private Official official;
    private Integer birthday;
    private Integer isTourist;
    private Integer isFakeAccount;
    private Integer pinPrompting;
    private Integer isDeleted;
    private Integer inRegAudit;
    private Boolean isRipUser;
    private Profession profession;
    private Integer faceNft;
    private Integer faceNftNew;
    private Integer isSeniorMember;
    private Honours honours;
    private String digitalId;
    private Integer digitalType;
    private LevelExp levelExp;
    private Double coins;
    private Integer following;
    private Integer follower;

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
    public static class Official {
        private Integer role;
        private String title;
        private String desc;
        private Integer type;
    }

    @NoArgsConstructor
    @Data
    public static class Profession {
        private Integer id;
        private String name;
        private String showName;
        private Integer isShow;
        private String categoryOne;
        private String realname;
        private String title;
        private String department;
    }

    @NoArgsConstructor
    @Data
    public static class Honours {
        private Integer mid;
        private Colour colour;
        private Object tags;

        @NoArgsConstructor
        @Data
        public static class Colour {
            private String dark;
            private String normal;
        }
    }

    @NoArgsConstructor
    @Data
    public static class LevelExp {
        private Integer currentLevel;
        private Integer currentMin;
        private Integer currentExp;
        private Integer nextExp;
        private Long levelUp;
    }
}
