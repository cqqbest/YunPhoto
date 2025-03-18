package com.cq.YunPhoto.Exception;

import lombok.Getter;


/**
 * 自定义异常
 */

@Getter
public class BusinessException extends RuntimeException{

    //自定义异常的新字段表示，表示错误码
    private int code;

    public BusinessException(int code,String message){
        super(message);
        this.code = code;
    }
    public BusinessException(ErrorCode errorCode,String message){
        super(message);
        this.code = errorCode.getCode();
    }
    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
