package io.cruii.bilibili.dto;

import cn.hutool.core.bean.BeanUtil;
import io.cruii.bilibili.vo.ContainerCardVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerDTO implements Serializable {
    private static final long serialVersionUID = -6073754165745971101L;

    private Integer dedeuserid;

    private String username;

    private String avatar;

    private String coins;

    private Integer level;

    private Integer currentExp;

    private Integer nextExp;

    private Integer vipType;

    private Long dueDate;

    private String key;

    private Boolean isLogin;

    public ContainerCardVO toCardVO() {
        ContainerCardVO cardVO = new ContainerCardVO();
        BeanUtil.copyProperties(this, cardVO);
        if (Boolean.FALSE.equals(this.isLogin)) {
            return cardVO;
        }
        cardVO.setDiffExp(this.level == 6 ? 0 : this.nextExp - this.currentExp);
        LocalDateTime dueLocalDateTime = LocalDateTime.ofEpochSecond(this.dueDate / 1000, 0, ZoneOffset.ofHours(9));
        cardVO.setIsVip(dueLocalDateTime.isAfter(LocalDateTime.now()));
        return cardVO;
    }
}
