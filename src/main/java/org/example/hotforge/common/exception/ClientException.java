package org.example.hotforge.common.exception;

import org.example.hotforge.common.result.ResultCode;


public class ClientException extends BaseException{
    public ClientException(ResultCode resultCode) {
        super(resultCode);
    }
    public ClientException(ResultCode resultCode,String message){
        super(resultCode,message);
    }
}
