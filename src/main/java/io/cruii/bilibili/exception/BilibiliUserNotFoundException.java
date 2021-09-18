package io.cruii.bilibili.exception;

/**
 * @author cruii
 * Created on 2021/9/15
 */
public class BilibiliUserNotFoundException extends RuntimeException {
    public BilibiliUserNotFoundException(String dedeuserid) {
        super("用户[" + dedeuserid + "]不存在");
    }
}
