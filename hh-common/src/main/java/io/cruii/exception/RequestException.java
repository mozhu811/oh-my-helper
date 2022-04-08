package io.cruii.exception;

/**
 * @author cruii
 * Created on 2021/12/27
 */
public class RequestException extends RuntimeException{
    public RequestException(String url, Throwable cause) {
        super("请求[" + url + "]失败", cause);
    }
}
