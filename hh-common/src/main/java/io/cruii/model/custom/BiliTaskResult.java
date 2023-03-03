package io.cruii.model.custom;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author cruii
 * Created on 2023/2/21
 */
@AllArgsConstructor
@Getter
public class BiliTaskResult {
    /**
     * 0 - fail
     * 1 - success
     */
    private int status;

    private final Object biliUser;

    private final Integer upgradeDays;
}
