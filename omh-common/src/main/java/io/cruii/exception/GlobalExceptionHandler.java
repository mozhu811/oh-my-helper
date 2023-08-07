package io.cruii.exception;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/9/23
 */
@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errorList = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errors", errorList);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleException(BaseException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(ex.getError().getStatus()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JSONObject> handleException(Exception ex, HttpServletRequest request) {
        JSONObject error = JSONUtil.createObj();
        error.set("message", "服务器好像出现了一些问题，请稍后再试");
        error.set("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.set("cause", ex.getMessage());
        error.set("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
