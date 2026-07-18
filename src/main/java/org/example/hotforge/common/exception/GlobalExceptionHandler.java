package org.example.hotforge.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.hotforge.common.result.Result;
import org.example.hotforge.common.result.ResultCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * ================================
 * 全局异常处理器
 * ================================
 * 拦截所有未捕获的异常，统一转换为 Result 响应。
 *
 * 处理优先级（Spring 按异常类型匹配，命中即停）：
 * 1. BaseException      → 我们自己的异常（Client / Server）
 * 2. @Valid 校验异常     → 请求参数不合法
 * 3. JSON 解析异常       → 请求体缺失或格式错误
 * 4. Exception          → 兜底，未预期的系统错误
 */
@Slf4j  //简化日志代码，类内直接使用 log 对象输出日志
@RestControllerAdvice
//全局拦截所有 @RestController 接口抛出的未捕获异常；
//自动将处理结果转为 JSON 响应，无需每个接口单独 try-catch；
//可限定扫描包、指定拦截控制器，默认全局生效。

public class GlobalExceptionHandler {
    // ==========================================
    // 自定义异常 — 所有 BaseException 子类一把抓
    // ==========================================
    /**
     * 拦截 ClientException + ServerException
     * ClientException 不记日志（用户侧问题）
     * ServerException   记 error 日志（我们侧问题，需排查）
     */

}
