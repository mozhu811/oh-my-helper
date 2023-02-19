package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class MangaSignErrorResponse {
    private String code;
    private String msg;
    private Meta meta;

    @NoArgsConstructor
    @Data
    public static class Meta {
        private String argument;
    }
}
