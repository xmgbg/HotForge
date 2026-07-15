package org.example.hotforge.common.exception;

import org.example.hotforge.common.result.ResultCode;
/**
 * ================================
 * 服务端异常
 * ================================
 * 包装底层基础设施故障或未预期的运行时错误，
 * 统一返回 500，完整堆栈必须记日志。
 *
 * 使用场景：
 * - Redis 连接失败、数据库查询异常
 * - 文件读写失败、第三方接口调用失败
 * - 任何 catch 到的底层异常需要往上抛时
 *
 * 例：
 * try {
 *     redisTemplate.opsForValue().get(key);
 * } catch (RedisConnectionFailureException e) {
 *     throw new ServerException("Redis 连接失败，key=" + key, e);
 * }
 */

public class ServerException extends BaseException {

    /**
     * 包装原始异常（保留完整堆栈用于排查）
     *
     * @param message 给日志看的描述，不暴露给前端
     * @param cause   原始异常
     */
    public ServerException(String message, Throwable cause) {
        super(ResultCode.INTERNAL_ERROR, message, cause);
    }

    /** 无原始异常时使用 */
    public ServerException(String message) {
        super(ResultCode.INTERNAL_ERROR, message);
    }

}
