package org.example.hotforge.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.hotforge.common.result.Result;
import org.example.hotforge.common.result.ResultCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        return Result.forbidden();
    }
    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBaseException(BaseException e, HttpServletRequest request) {
        if (e instanceof ServerException) {
            log.error("[服务端异常] uri={}, msg={}", request.getRequestURI(), e.getMessage(), e);
            // 内部异常信息可能包含敏感数据，仅返回通用文案。
            return Result.error(ResultCode.INTERNAL_ERROR);
        }
        return Result.error(e.getResultCode(), e.getMessage());
    }


    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("[参数校验失败] uri={}, msg={}", request.getRequestURI(), message);
        return Result.fail(message);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e,
                                                 HttpServletRequest request) {
        log.warn("[请求体解析失败] uri={}", request.getRequestURI());
        return Result.fail("请求体缺失或 JSON 格式错误");
    }


    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("[未预期异常] uri={}", request.getRequestURI(), e);
        return Result.error(ResultCode.INTERNAL_ERROR);
    }


}
