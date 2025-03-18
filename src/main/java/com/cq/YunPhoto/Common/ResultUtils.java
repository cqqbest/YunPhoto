package com.cq.YunPhoto.Common;


import com.cq.YunPhoto.Exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * 封装的返回响应的方法（与ThrowUtils类似）
 */
@Component
public class ResultUtils {
    /**
     * 成功
     * @param data 返回的数据
     * @return响应
     * @param <T> 数据类型
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<T>(0,data,"ok");
    }

    /**
     * 失败
     * @param code 错误码
     * @param message 错误信息
     * @return 响应
     */
    public static BaseResponse<?> error(int code,String message){
        return new BaseResponse<>(code,null,message);
    }

    /**
     * 失败
     * @param errorCode 自定义异常
     * @param message 补充的异常信息
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message){
        return new BaseResponse<>(errorCode.getCode(),null,message);
    }

    /**
     * 失败
     * @param errorCode 自定义异常
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }
}
