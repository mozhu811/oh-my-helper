package io.cruii.bilibili.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2021/6/10
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BilibiliUserVO implements Serializable {
    private static final long serialVersionUID = -4960479906705315906L;

    private String username;

    private String avatar;

    private Integer level;
}
