package io.cruii.bilibili.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/6/8
 */
@Data
public class ContainerConfig implements Serializable {

    private static final long serialVersionUID = 6151419899088396002L;

    private Integer dedeuserid;

    private String sessdata;

    private String biliJct;

    private Boolean bet;

    /**
     * 任务之间的执行间隔,默认10秒,云函数用户不建议调整的太长，注意免费时长。
     */
    private Integer taskIntervalTime;

    /**
     * 每日投币数量,默认 5 ,为 0 时则不投币
     * 范围: [0, 5]
     */
    private Integer numberOfCoins;

    /**
     * 预留的硬币数，当硬币余额小于这个值时，不会进行投币任务，默认值为 50
     * 范围: [0, 4000]
     */
    private Integer reserveCoins;

    /**
     * 投币时是否点赞, 默认 0
     * 0：否
     * 1：是
     */
    private Integer selectLike;

    /**
     * 年度大会员月底是否用 B 币券给自己充电
     * 默认 true，即充电对象是你本人。
     */
    private Boolean monthEndAutoCharge;

    /**
     * 直播送出即将过期的礼物，默认开启
     * 如需关闭请改为 false
     */
    private Boolean giveGift;

    /**
     * 送礼 up 主的 uid
     * 直播送出即将过期的礼物，指定 up 主，
     * 为 0 时则随随机选取一个 up 主
     */
    private String upLive;

    /**
     * 充电对象的 uid
     * 给指定 up 主充电，默认为 0，即给自己充电。
     */
    private String chargeForLove;

    /**
     * 手机端漫画签到时的平台，建议选择你设备的平台
     * ios 或者 android
     * 默认 ios
     */
    private String devicePlatform;

    /**
     * 0：优先给热榜视频投币，1：优先给关注的 up 投币
     */
    private Integer coinAddPriority;

    /**
     * 浏览器 UA
     */
    private String userAgent;

    /**
     * 是否跳过每日任务，默认true
     */
    private Boolean skipDailyTask;

    /**
     * TelegramBot token
     */
    private String telegrambottoken;

    /**
     * TelegranBot ChatId
     */
    private String telegramchatid;

    /**
     * Server酱推送Key
     */
    private String serverpushkey;

    /**
     * 电子邮箱
     */
    private String email;
}
