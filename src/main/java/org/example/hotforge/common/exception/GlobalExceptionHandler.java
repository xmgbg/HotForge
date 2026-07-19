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
    // 1.自定义异常 — 所有 BaseException 子类一把抓
    // ==========================================
    /**
     * 拦截 ClientException + ServerException
     * ClientException 不记日志（用户侧问题）
     * ServerException   记 error 日志（我们侧问题，需排查）
     */
    @ExceptionHandler(BaseException.class)   // 声明：本方法处理 BaseException 及其所有子类
    public Result<Void> handleBaseException(BaseException e, HttpServletRequest request) {
        // request 参数由 Spring 自动注入 → 用来拿到"哪个接口炸了"
        if (e instanceof ServerException) {  // ── 服务端异常：我们的锅 ──
            // 最后一个参数传 e：日志会打印完整堆栈（含构造时保留的 cause 链），排查全靠它
            log.error("[服务端异常] uri={}, msg={}", request.getRequestURI(), e.getMessage(), e);
            // 注意：ServerException 的 message 是"给日志看的"（可能含 Redis key、SQL 等内部细节）
            // 绝不能返给前端 → 只返回枚举默认文案"服务器内部错误"
            return Result.error(ResultCode.INTERNAL_ERROR);
        }
        // ── 走到这说明是 ClientException：用户操作不合规，可预期 → 不记日志 ──
        // message 直接翻译给前端看（"手机号已注册" / "课程名额已满" ...）
        return Result.error(e.getResultCode(), e.getMessage());
    }

    // ==========================================
    // 2. @Valid 参数校验失败
    // ==========================================

    /**
     * 是什么：DTO 字段上标了 @NotBlank/@Min 等注解，@Valid 校验没过时 Spring 抛的异常
     * 小知识：MethodArgumentNotValidException（@RequestBody 场景）继承自
     *         BindException（表单/URL参数场景），所以参数声明为父类 BindException，
     *         一个方法就能同时接住两种
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult()       // 拿到校验结果集（可能多个字段同时不合格）
                .getFieldErrors()                   // 取出所有"字段级"错误
                .stream()                           // 转成流，逐个加工
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                // 每条拼成 "phone 手机号格式不正确"
                .collect(Collectors.joining("; ")); // 多条错误用分号连成一句话
        // 前端传参问题记 warn 即可（不是故障，但联调时能帮前端发现 bug），不打堆栈
        log.warn("[参数校验失败] uri={}, msg={}", request.getRequestURI(), message);
        return Result.fail(message);                // fail() = 400 + 自定义文案
    }

    // ==========================================
    // 3. 请求体缺失 / JSON 格式错误
    // ==========================================

    /**
     * 是什么：前端没传 body、JSON 少引号逗号、字段类型对不上（"abc" 传给 int）时，
     *         Jackson 反序列化失败抛的异常 —— 发生在进 Controller 之前，@Valid 还没机会跑
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e,
                                                 HttpServletRequest request) {
        log.warn("[请求体解析失败] uri={}", request.getRequestURI());
        // 原始 message 是 Jackson 的英文报错（还可能带类名），前端看不懂 → 换成人话
        return Result.fail("请求体缺失或 JSON 格式错误");
    }

    // ==========================================
    // 4. 兜底 — 所有没被上面接住的异常
    // ==========================================

    /**
     * 是什么：最后一道防线（NPE、数组越界、没被包装的底层异常 ...）
     * 为什么：没有它，未知异常会以 Spring 默认的 500 错误页裸奔给前端
     * 注意：Spring 按"最具体的类型"匹配 handler，跟方法书写顺序无关，
     *       所以这个 Exception 兜底不会抢走上面三个的活
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        // 未预期异常 = 潜在 Bug，必须 error + 完整堆栈
        log.error("[未预期异常] uri={}", request.getRequestURI(), e);
        // 同样只返回通用文案，绝不把 e.getMessage() 泄漏给前端（可能含 SQL、文件路径等敏感信息）
        return Result.error(ResultCode.INTERNAL_ERROR);
    }


//    所有类极简作用
//    ResultCode：错误标签枚举，存错误码和默认提示，全程传递
//    BaseException/ClientException/ServerException
//    自定义异常，3 个构造方法只在 Service 层 new 异常时调用，用来把枚举、提示、底层异常打包存进异常对象 e
//    GlobalExceptionHandler：全局抓异常，用 instanceof 区分是客户端 / 服务端异常，控制日志和返回文案
//    Result：统一返回格式，Result.error() 是静态工具方法，只在全局异常类 return 的时候调用，生成给前端的失败 JSON
//    二、你最懵的两个点单独讲透
//1）异常的三个构造方法：在哪用、干嘛用
//    使用位置：Service 业务层，写 throw new ClientException() / throw new ServerException() 这一刻
//    作用：创建异常对象 e，把枚举、提示文字、底层报错存到 e 里面，后面全局异常才能读取错误信息
//    构造 1（仅枚举）：简单错误，直接用枚举默认提示
//    构造 2（枚举 + 自定义文字）：客户端异常，给用户写专属提示（手机号错误）
//    构造 3（枚举 + 文字 + 原始异常）：服务端异常，保存底层报错，打印日志堆栈
//2）Result.error ()：在哪用、干嘛用
//    不是构造方法，是 Result 类静态工具
//    使用位置：仅在 GlobalExceptionHandler 处理方法的 return 处调用
//    两个重载：
//            Result.error (枚举)：服务端异常专用，只拿枚举默认提示，隐藏内部报错
//Result.error (枚举，自定义文字)：客户端异常专用，展示自定义友好提示
//    作用：自动封装标准返回体 code、msg、data=null，直接转 JSON 发给前端
//    三、完整流程（客户端异常：手机号格式错误）
//    Service 层校验失败：throw new ClientException(ResultCode.PARAM_ERROR,"手机号格式不正确");
//    触发异常构造方法，生成异常对象 e，存入枚举和提示文字
//    异常被全局异常处理器捕获，Spring 传入 e、request
//    e instanceof ServerException 判断 false，客户端异常
//    执行 return Result.error(e.getResultCode(), e.getMessage());
//    调用 Result.error 静态方法，组装失败返回对象
//    返回 JSON 给前端展示错误
//    四、服务端异常简短流程（数据库报错）
//    Service 捕获 SQL 异常：throw new ServerException(ResultCode.INTERNAL_ERROR,"查询失败",sql原始异常);
//    调用三号构造，存入枚举、日志文案、底层异常
//    全局捕获，instanceof 判定是服务端异常
//    log.error 打印接口地址、完整异常堆栈
//    执行 return Result.error(ResultCode.INTERNAL_ERROR);
//    调用单参数 error，返回统一 500 兜底提示











}
