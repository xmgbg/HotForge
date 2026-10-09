package org.example.hotforge.common.result;

public enum ResultCode {
    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),

    INTERNAL_ERROR(500, "服务器内部错误"),

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
