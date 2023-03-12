package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class BiliDailyReward {
    private Boolean login;
    private Boolean watch;
    private Integer coins;
    private Boolean share;
    private Boolean email;
    private Boolean tel;
    private Boolean safeQuestion;
    private Boolean identifyCard;
}
