package io.cruii.bilibili.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;


/**
 * @author cruii
 * Created on 2021/9/14
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "bilibili_user")
public class BilibiliUser implements Serializable {
    private static final long serialVersionUID = -52229197213938647L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dedeuserid;

    private String username;

    private String coins;

    private Integer level;

    private Integer currentExp;

    private Integer nextExp;

    private Integer upgradeDays;

    private String medals;

    private Integer vipType;

    private Integer vipStatus;

    private Boolean isLogin;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BilibiliUser that = (BilibiliUser) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getDedeuserid(), that.getDedeuserid()) && Objects.equals(getUsername(), that.getUsername()) && Objects.equals(getCoins(), that.getCoins()) && Objects.equals(getLevel(), that.getLevel()) && Objects.equals(getCurrentExp(), that.getCurrentExp()) && Objects.equals(getNextExp(), that.getNextExp()) && Objects.equals(getUpgradeDays(), that.getUpgradeDays()) && Objects.equals(getMedals(), that.getMedals()) && Objects.equals(getVipType(), that.getVipType()) && Objects.equals(getVipStatus(), that.getVipStatus()) && Objects.equals(getIsLogin(), that.getIsLogin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDedeuserid(), getUsername(), getCoins(), getLevel(), getCurrentExp(), getNextExp(), getUpgradeDays(), getMedals(), getVipType(), getVipStatus(), getIsLogin());
    }
}
