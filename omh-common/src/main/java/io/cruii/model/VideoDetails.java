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
public class VideoDetails {
    private String bvid;
    private Integer aid;
    private Integer videos;
    private Integer tid;
    private String tname;
    private Integer copyright;
    private String pic;
    private String title;
    private Integer pubdate;
    private Integer ctime;
    private String desc;
    private List<?> descV2;
    private Integer state;
    private Integer duration;
    private String redirectUrl;
    private Rights rights;
    private Owner owner;
    private Stat stat;
    private String dynamic;
    private Integer cid;
    private Dimension dimension;
    private Object premiere;
    private Integer teenageMode;
    private Boolean isChargeableSeason;
    private Boolean isStory;
    private Boolean noCache;
    private List<?> pages;
    private Subtitle subtitle;
    private Boolean isSeasonDisplay;
    private UserGarb userGarb;
    private HonorReply honorReply;
    private String likeIcon;
    private Boolean needJumpBv;

    @NoArgsConstructor
    @Data
    public static class Rights {
    }

    @NoArgsConstructor
    @Data
    public static class Owner {
    }

    @NoArgsConstructor
    @Data
    public static class Stat {
    }

    @NoArgsConstructor
    @Data
    public static class Dimension {
    }

    @NoArgsConstructor
    @Data
    public static class Subtitle {
    }

    @NoArgsConstructor
    @Data
    public static class UserGarb {
    }

    @NoArgsConstructor
    @Data
    public static class HonorReply {
    }
}
