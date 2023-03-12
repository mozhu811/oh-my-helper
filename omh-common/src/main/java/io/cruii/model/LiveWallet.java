package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class LiveWallet {
    private Integer gold;
    private Integer silver;
    private String bp;
    private Integer metal;
    private Boolean needUseNewBp;
    private Integer iosBp;
    private Integer commonBp;
    private String newBp;
    private Integer bp2GoldAmount;
}
