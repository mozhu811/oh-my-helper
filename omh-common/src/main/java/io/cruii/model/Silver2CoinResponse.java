package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class Silver2CoinResponse {
    private Integer coin;
    private Integer gold;
    private Integer silver;
    private String tid;
}
