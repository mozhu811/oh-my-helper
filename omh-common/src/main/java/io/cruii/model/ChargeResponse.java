package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class ChargeResponse {
    private Integer mid;
    private Integer upMid;
    private String orderNo;
    private String bpNum;
    private Integer exp;
    private Integer status;
    private String msg;
}
