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
public class BiliCoinLog {
    private List<CoinLog> list;
    private Integer count;

    @NoArgsConstructor
    @Data
    public static class CoinLog {
        private String time;
        private Integer delta;
        private String reason;
    }
}
