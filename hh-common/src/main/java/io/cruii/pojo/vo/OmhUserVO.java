package io.cruii.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2023/2/17
 */
@Data
@Accessors(chain = true)
public class OmhUserVO implements Serializable {
    private static final long serialVersionUID = -4046583379932585203L;

    private String userId;

    private String nickname;

    private Long biliTaskConfigId;
}
