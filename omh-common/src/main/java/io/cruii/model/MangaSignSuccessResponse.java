package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class MangaSignSuccessResponse {
    private Integer code;
    private String msg;
    private Data data;

    @NoArgsConstructor
    @lombok.Data
    public static class Data {
    }
}
