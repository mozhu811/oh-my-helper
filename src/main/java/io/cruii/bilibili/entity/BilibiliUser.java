package io.cruii.bilibili.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.util.List;


/**
 * @author cruii
 * Created on 2021/9/14
 */
@Data
@Document("bilibili_user")
public class BilibiliUser implements Serializable {
    private static final long serialVersionUID = -52229197213938647L;

    @MongoId
    private String dedeuserid;

    private String username;

    private String avatar;

    private String coins;

    private Integer level;

    private Integer currentExp;

    private Integer nextExp;

    private List<Medal> medals;

    private Integer vipType;

    private Long dueDate;

    private Integer vipStatus;

    private Boolean isLogin;
}
