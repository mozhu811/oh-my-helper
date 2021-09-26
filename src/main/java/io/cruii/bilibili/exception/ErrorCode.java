package io.cruii.bilibili.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author cruii
 * Created on 2021/9/23
 */
@Getter
public enum ErrorCode {
    COOKIE_EXPIRED(101, HttpStatus.UNAUTHORIZED, "\uD83D\uDE35 Oops! 账号Cookie已失效"),
    B_USER_NOT_FOUND(102, HttpStatus.NOT_FOUND, "\uD83D\uDE35\u200D\uD83D\uDCAB 用户未找到");

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    private final int code;

    private final HttpStatus status;

    private final String message;
}
