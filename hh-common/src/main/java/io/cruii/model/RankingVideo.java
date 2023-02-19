package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class RankingVideo {
    private String aid;
    private String bvid;
    private String typename;
    private String title;
    private String subtitle;
    private Integer play;
    private Integer review;
    private Integer videoReview;
    private Integer favorites;
    private Integer mid;
    private String author;
    private String description;
    private String create;
    private String pic;
    private Integer coins;
    private String duration;
    private Boolean badgepay;
    private Integer pts;
    private Rights rights;
    private String redirectUrl;

    @NoArgsConstructor
    @Data
    public static class Rights {
        private Integer bp;
        private Integer elec;
        private Integer download;
        private Integer movie;
        private Integer pay;
        private Integer hd5;
        private Integer noReprint;
        private Integer autoplay;
        private Integer ugcPay;
        private Integer isCooperation;
        private Integer ugcPayPreview;
        private Integer noBackground;
        private Integer arcPay;
        private Integer payFreeWatch;
    }
}
