package org.example.hotforge.common.exception;

import org.example.hotforge.common.result.ResultCode;

/**
 * ================================
 * 客户端异常
 * ================================
 * 用户操作不合规 → Service 层判断后主动抛出。
 * 可预期的 → 不记日志，直接翻译给前端看具体原因。
 *
 * 使用场景：
 * - 手机号已注册、验证码错误、密码不对
 * - 课程满了、重复预约、超时取消
 * - 没有权限、教练未认证
 *
 * 例：
 * throw new ClientException(ResultCode.COURSE_FULL);
 * throw new ClientException(ResultCode.PHONE_EXISTS, "该手机号已于
 2025-03-12 注册");
 */

public class ClientException extends BaseException{
    public ClientException(ResultCode resultCode) {
        super(resultCode);
    }
    public ClientException(ResultCode resultCode,String message){
        super(resultCode,message);
    }
}
