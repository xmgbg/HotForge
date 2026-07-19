package org.example.hotforge.common.result;

import lombok.Data;

/**
 * ================================
 * 统一响应体 Result<T>
 * ================================
 * 所有 Controller 返回值统一包装为此类型，
 * 前端根据 code 判断业务状态。
 * Integer：java.lang 下的包装类，是类，对象，默认值 null
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
// ──────────────────────────────────
// 构造器（私有，强制走静态工厂方法）
// ──────────────────────────────────
    private Result(){
    }
    private Result(Integer code,String message,T data){
        this.code=code;
        this.message=message;
        this.data=data;
    }

    // ==========================================
    // 成功响应
    // ==========================================
    /** 成功，无数据（如删除操作） */
//    public static
//    公共静态方法，无需创建 Result 对象，直接通过类名调用。
//<T>
//    泛型声明：定义泛型类型 T，表示该方法支持任意数据类型。
//    写在方法返回值前面，是方法级泛型。
//    Result<T>
//    方法返回值类型：返回一个 Result 实体类对象，且该对象内部承载的数据类型为 T。
//    success()
//    方法名，无参；一般用于构建通用成功响应（无返回数据，只返回状态码、提示信息）。
    public static <T> Result<T> success(){
        return new Result<>(ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getDefaultMessage(),null);
    }
    /** 成功，有数据（如查询操作） */
    public static <T> Result<T> success(T data){
        return new Result<>(ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getDefaultMessage(),data);
    }
    /** 成功，自定义消息 + 数据 */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ==========================================
    // 失败响应
    // ==========================================
    /** 使用枚举自带的默认文案 */
    public static <T> Result<T> error(ResultCode resultCode){
        return new Result<>(resultCode.getCode(),resultCode.getDefaultMessage(), null);
    }
    /** 覆盖枚举默认文案，传自定义 message */
    public static <T> Result<T> error(ResultCode resultCode,String message) {
        return new Result<>(resultCode.getCode(),message,null);
    }
    // ==========================================
    // 快捷方法（高频场景，内部委托给 error()）
    // ==========================================
    /** 参数校验失败（400） */
    public static <T> Result<T> fail(String message) {
        return error(ResultCode.BAD_REQUEST, message);
    }

    /** 未登录/token 过期（401） */
    public static <T> Result<T> unauthorized() {
        return error(ResultCode.UNAUTHORIZED);
    }

    /** 资源冲突/重复操作（409） */
    /** 权限不足（403） */
    public static <T> Result<T> forbidden() {
        return error(ResultCode.FORBIDDEN);
    }

    /** 资源不存在（404） */
    public static <T> Result<T> notFound() {
        return error(ResultCode.NOT_FOUND);
    }

}



