package org.example.hotforge.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.hotforge.common.result.Result;
import org.example.hotforge.common.result.ResultCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ================================
 * 全局异常处理器
 * ================================
 * 拦截所有未捕获的异常，统一转换为 Result 响应。
 *
 * 处理优先级（Spring 按异常类型匹配，命中即停）：
 * 1. BaseException      → 我们自己的异常（Client / Server）
 * 2. @Valid 校验异常     → 请求参数不合法
 * 3. Exception          → 兜底，未预期的系统错误
 */
@Slf4j
@RestControllerAdvice
//全局 REST 控制器增强注解，Spring MVC 提供。
//等价组合：@ControllerAdvice + @ResponseBody
//拦截项目中所有标记了 @RestController 的接口，是全局级统一处理器，不用在每个接口单独写异常处理。
//搭配 @ExceptionHandler 使用（最主流场景）
//专门用来全局捕获接口抛出的异常，统一封装返回格式，替代接口内零散的 try-catch。
//自带 @ResponseBody 效果，处理方法返回对象会自动转为 JSON，直接给到前端。
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
