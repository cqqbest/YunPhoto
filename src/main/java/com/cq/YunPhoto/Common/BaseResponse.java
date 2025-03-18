package com.cq.YunPhoto.Common;

import com.cq.YunPhoto.Exception.ErrorCode;
import lombok.Data;

/**
 * 响应包装类（通用返回）
 * @param <T>
 */

@Data
public class BaseResponse<T> {
    private  int code;
    //响应返回的数据类型用泛型
    private  T data;
    private  String message;

    public BaseResponse(int code,T data,String message){
        this.code = code;
        this.data = data;
        this.message = message;
    }
    public BaseResponse(int code,T data){
        this(code,data, " ");
    }
    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null,errorCode.getMessage());
    }

}
