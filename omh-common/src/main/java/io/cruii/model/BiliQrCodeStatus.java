package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class BiliQrCodeStatus {
    private String url;
    private String refreshToken;
    private Integer timestamp;
    private Integer code;
    private String message;
}
