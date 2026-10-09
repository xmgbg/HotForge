package org.example.hotforge.common.exception;

import lombok.Getter;
import org.example.hotforge.common.result.ResultCode;

@Getter
public abstract class BaseException extends RuntimeException{
    /** 业务状态码 + 默认消息 */
    private final ResultCode resultCode;


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
