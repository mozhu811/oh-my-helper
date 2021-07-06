package io.cruii.bilibili.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/7/6
 */
@Data
public class ContainerCookieVO implements Serializable {
    private static final long serialVersionUID = 4526030632788216062L;

    private Integer dedeuserid;

    private String sessdata;

    private String biliJct;

}
