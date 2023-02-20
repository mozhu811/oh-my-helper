package io.cruii.model.custom;

import io.cruii.model.BiliUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author cruii
 * Created on 2023/2/21
 */
@AllArgsConstructor
@Getter
public class BiliUserAndDays {
    private final BiliUser biliUser;

    private final int upgradeDays;
}
