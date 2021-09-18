package io.cruii.bilibili.exception;

import lombok.Getter;

/**
 * @author cruii
 * Created on 2021/9/14
 */
public class BilibiliCookieExpiredException extends RuntimeException {
    @Getter
    private final String dedeuserid;

    public BilibiliCookieExpiredException(String dedeuserid) {
        super("Bilibili账号[" + dedeuserid + "]Cookie已失效");
        this.dedeuserid = dedeuserid;
    }
}
