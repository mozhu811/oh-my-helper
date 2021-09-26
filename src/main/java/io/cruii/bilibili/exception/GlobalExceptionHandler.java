package io.cruii.bilibili.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @author cruii
 * Created on 2021/9/23
 */
@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleException(BaseException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex, request.getRequestURI());
        log.error(ex);
        return ResponseEntity.status(ex.getError().getStatus()).body(errorResponse);
    }
}
