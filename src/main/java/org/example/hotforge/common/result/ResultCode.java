package org.example.hotforge.common.result;
/**
 * ================================
 * 统一响应状态码枚举
 * ================================
 * 枚举是一种自定义常量类型，用于限定变量取值范围，把固定、有限的取值一一列举出来
 * 规范：成功 2xx，客户端错误 4xx，服务端错误 5xx，业务异常 1xxx+
 * 每个枚举值携带 code（返回给前端）和 defaultMessage（默认提示文案）
 */

public enum ResultCode {
    // ──────────────────────────────────
    // 成功 调用枚举的私有构造方法来创建实例对象
    // ──────────────────────────────────
    SUCCESS(200, "操作成功"),

    // ──────────────────────────────────
    // 客户端错误
    // ──────────────────────────────────
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),

    // ──────────────────────────────────
    // 服务端错误
    // ──────────────────────────────────
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ──────────────────────────────────
    // 业务异常（1xxx 起）
    // ──────────────────────────────────
    PHONE_EXISTS(1001, "手机号已注册"),
    CODE_INVALID(1002, "验证码错误"),
    LOGIN_FAILED(1003, "账号或密码错误"),
    ACCOUNT_DISABLED(1004, "账号已禁用"),
    VIP_EXPIRED(1005, "VIP 已过期"),
    COURSE_FULL(1006, "课程名额已满"),
    DUPLICATE_BOOKING(1007, "重复预约"),
    CANCEL_TIMEOUT(1008, "超过取消时间限制"),
    TRAINER_NOT_APPROVED(1009, "教练未通过认证");

    private  final int code;
    private final String defaultMessage;

    ResultCode(int code,String defaultMessage){
        this.code=code;
        this.defaultMessage=defaultMessage;
    }

    public int getCode(){
        return code;
    }

    public String getDefaultMessage(){
        return defaultMessage;
    }
}