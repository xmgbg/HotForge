package org.example.hotforge.common.exception;

import org.example.hotforge.common.result.ResultCode;

public class ServerException extends BaseException {

    public ServerException(String message, Throwable cause) {
        super(ResultCode.INTERNAL_ERROR, message, cause);
    }

    /** 无原始异常时使用 */
    public ServerException(String message) {
        super(ResultCode.INTERNAL_ERROR, message);
    }

}
