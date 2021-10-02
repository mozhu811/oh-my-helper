package io.cruii.bilibili.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author cruii
 * Created on 2021/6/8
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "task_config")
public class TaskConfig implements Serializable {

    private static final long serialVersionUID = 6151419899088396002L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * B站uid
     */
    private String dedeuserid;

    /**
     * 会话标识
     */
    private String sessdata;

    /**
     * csrf校验
     */
    private String biliJct;

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
     * B站推送
     */
    private Boolean biliPush;

    /**
     * 关注开发者
     */
    private Boolean followDeveloper;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskConfig that = (TaskConfig) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getDedeuserid(), that.getDedeuserid()) && Objects.equals(getSessdata(), that.getSessdata()) && Objects.equals(getBiliJct(), that.getBiliJct()) && Objects.equals(getDonateCoins(), that.getDonateCoins()) && Objects.equals(getReserveCoins(), that.getReserveCoins()) && Objects.equals(getAutoCharge(), that.getAutoCharge()) && Objects.equals(getDonateGift(), that.getDonateGift()) && Objects.equals(getDonateGiftTarget(), that.getDonateGiftTarget()) && Objects.equals(getAutoChargeTarget(), that.getAutoChargeTarget()) && Objects.equals(getDevicePlatform(), that.getDevicePlatform()) && Objects.equals(getDonateCoinStrategy(), that.getDonateCoinStrategy()) && Objects.equals(getUserAgent(), that.getUserAgent()) && Objects.equals(getSkipTask(), that.getSkipTask()) && Objects.equals(getTgBotToken(), that.getTgBotToken()) && Objects.equals(getTgBotChatId(), that.getTgBotChatId()) && Objects.equals(getScKey(), that.getScKey()) && Objects.equals(getCorpId(), that.getCorpId()) && Objects.equals(getAgentId(), that.getAgentId()) && Objects.equals(getCorpSecret(), that.getCorpSecret()) && Objects.equals(getMediaId(), that.getMediaId()) && Objects.equals(getBiliPush(), that.getBiliPush()) && Objects.equals(getFollowDeveloper(), that.getFollowDeveloper());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDedeuserid(), getSessdata(), getBiliJct(), getDonateCoins(), getReserveCoins(), getAutoCharge(), getDonateGift(), getDonateGiftTarget(), getAutoChargeTarget(), getDevicePlatform(), getDonateCoinStrategy(), getUserAgent(), getSkipTask(), getTgBotToken(), getTgBotChatId(), getScKey(), getCorpId(), getAgentId(), getCorpSecret(), getMediaId(), getBiliPush(), getFollowDeveloper());
    }
}
