package io.cruii.exception;

/**
 * @author cruii
 * Created on 2021/9/14
 */
public class BilibiliCookieExpiredException extends BaseException {
    public BilibiliCookieExpiredException(String dedeuserid) {
        super(ErrorCode.COOKIE_EXPIRED, "Bilibili账号[" + dedeuserid + "]Cookie已失效");
    }
}
