package io.cruii.bilibili.vo;

import lombok.*;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/08/13
 */
@Data
public class QrCodeVO implements Serializable {
    private static final long serialVersionUID = 9000856617199889458L;

    private String qrCodeUrl;

    private String qrCodeImg;

    private String oauthKey;
}
