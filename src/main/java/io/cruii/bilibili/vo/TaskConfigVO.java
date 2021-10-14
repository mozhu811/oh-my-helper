package io.cruii.bilibili.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/9/14
 */
@Data
public class TaskConfigVO implements Serializable {
    private static final long serialVersionUID = 4344616448330555244L;

    /**
     * 每日投币数量,默认 5 ,为 0 时则不投币
     * 范围: [0, 5]
     */
    private Integer donateCoins;

    /**
     * 预留的硬币数，当硬币余额小于这个值时，不会进行投币任务，默认值为 50
     * 范围: [0, 4000]
     */
    private Integer reserveCoins;

    /**
     * 年度大会员月底是否用 B 币券给自己充电
     * 默认 true，即充电对象是你本人。
     */
    private Boolean autoCharge;

    /**
     * 直播送出即将过期的礼物，默认开启
     * 如需关闭请改为 false
     */
    private Boolean donateGift;

    /**
     * 送礼 up 主的 uid
     * 直播送出即将过期的礼物，指定 up 主，
     */
    private String donateGiftTarget;

    /**
     * 充电对象的 uid
     */
    private String autoChargeTarget;

    /**
     * 手机端漫画签到时的平台，建议选择你设备的平台
     * ios 或者 android
     * 默认 ios
     */
    private String devicePlatform;

    /**
     * 0：优先给热榜视频投币，1：优先给关注的 up 投币
     */
    private Integer donateCoinStrategy;

    /**
     * 浏览器 UA
     */
    private String userAgent;

    /**
     * 是否跳过每日任务，默认true
     */
    private Boolean skipTask;

    /**
     * TelegramBot token
     */
    private String tgBotToken;

    /**
     * TelegranBot ChatId
     */
    private String tgBotChatId;

    /**
     * Server酱推送Key
     */
    private String scKey;

    /**
     * 企业微信 corpId
     */
    private String corpId;

    /**
     * 企业微信 agentId
     */
    private String agentId;

    /**
     * 企业微信 corpSecret
     */
    private String corpSecret;

    /**
     * 企业微信图文推送 mediaId
     */
    private String mediaId;

    /**
     * Bark 推送token
     */
    private String barkToken;

    /**
     * B站推送
     */
    private Boolean biliPush;

    /**
     * 关注开发者
     */
    private Boolean followDeveloper;
}
