package io.cruii.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class ShareVideoResponse extends BiliBaseResponse{
    private Integer data;
}
