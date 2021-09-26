package io.cruii.bilibili.exception;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

/**
 * @author cruii
 * Created on 2021/9/23
 */
@Data
public class ErrorResponse implements Serializable {
    private static final long serialVersionUID = 5966145696474913450L;

    private int code;
    private int status;
    private String message;
    private String path;
    private Instant timestamp;

    public ErrorResponse(BaseException ex, String path) {
        this(ex.getError().getCode(), ex.getError().getStatus().value(), ex.getError().getMessage(), path);
    }

    public ErrorResponse(int code, int status, String message, String path) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }
}
