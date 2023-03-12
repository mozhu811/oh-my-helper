package io.cruii.exception;

/**
 * @author cruii
 * Created on 2021/9/15
 */
public class BilibiliUserNotFoundException extends BaseException {
    public BilibiliUserNotFoundException(String dedeuserid) {
        super(ErrorCode.B_USER_NOT_FOUND, "用户[" + dedeuserid + "]不存在");
    }
}
