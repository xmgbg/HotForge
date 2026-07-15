package org.example.hotforge.common.exception;

import lombok.Getter;
import org.example.hotforge.common.result.ResultCode;
/**
 * ================================
 * 异常基类
 * ================================
 * 所有自定义异常的公共父类，携带 ResultCode 信息，
 * 由 GlobalExceptionHandler 统一拦截 → Result 响应。
 *
 * 子类分工：
 * - ClientException  客户端异常（用户操作不合规，可预期，不记日志）
 * - ServerException  服务端异常（基础设施故障/代码 Bug，必须记日志）
 */

@Getter
public abstract class BaseException extends RuntimeException{
    /** 业务状态码 + 默认消息 */
    private final ResultCode resultCode;

    // ──────────────────────────────────
    // 构造器（protected，只有子类能用）
    // ──────────────────────────────────

    /** 使用枚举默认文案 */
    protected BaseException(ResultCode resultcode) {
        super(resultcode.getDefaultMessage());
        this.resultCode = resultcode;
    }

    /** 覆盖默认文案 */
    protected BaseException(ResultCode resultcode, String message) {
        super(message);
        this.resultCode = resultcode;
    }

    /** 包装原始异常，保留完整堆栈 */
    protected BaseException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }

    /** 快捷获取状态码数值 */
    public int getCode(){
        return resultCode.getCode();
    }
}
