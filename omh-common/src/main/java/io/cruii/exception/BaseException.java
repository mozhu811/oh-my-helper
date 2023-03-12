package io.cruii.exception;

import lombok.Getter;

/**
 * @author cruii
 * Created on 2021/9/23
 */
@Getter
public abstract class BaseException extends RuntimeException{
    private final ErrorCode error;
    private final String message;

    protected BaseException(ErrorCode error, String message) {
        super(error.getMessage());
        this.error = error;
        this.message = message;
    }

}
