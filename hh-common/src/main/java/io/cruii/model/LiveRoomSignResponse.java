package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class LiveRoomSignResponse {
    /**
     * text
     */
    private String text;
    /**
     * specialText
     */
    private String specialText;
    /**
     * allDays
     */
    private Integer allDays;
    /**
     * hadSignDays
     */
    private Integer hadSignDays;
    /**
     * isBonusDay
     */
    private Integer isBonusDay;
}
