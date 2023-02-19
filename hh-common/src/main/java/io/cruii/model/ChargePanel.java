package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class ChargePanel {
    private BpWallet bpWallet;
    private List<BatteryList> batteryList;

    @NoArgsConstructor
    @Data
    public static class BpWallet {
        private Integer defaultBp;
        private Integer iosBp;
        private Integer couponBalance;
        private Integer availableBp;
        private Integer unavailableBp;
        private Integer totalBp;
        private Integer mid;
        private String unavailableReason;
        private Boolean isBpRemainsPrior;
        private String tip;
    }

    @NoArgsConstructor
    @Data
    public static class BatteryList {
        private String title;
        private Integer groups;
        private Integer isCustomize;
        private Integer elecNum;
        private Integer minElec;
        private Integer maxElec;
        private Integer isChecked;
        private Integer seq;
        private String bpNum;
        private String minBp;
        private String maxBp;
    }
}
