package io.cruii.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/7/6
 */
@Data
public class BilibiliLoginVO implements Serializable {
    private static final long serialVersionUID = 4526030632788216062L;

    /**
     * B站官方扫码登录返回的状态码
     */
    private Integer code;

    private Integer dedeuserid;

    private String sessdata;

    private String biliJct;

}
