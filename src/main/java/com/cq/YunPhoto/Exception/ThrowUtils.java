package com.cq.YunPhoto.Exception;


/**
 * 封装的异常抛出方法（类似于断言，方便后续抛出异常）
 */

public class ThrowUtils {
    public static void throwIf(boolean tiaojian,RuntimeException runtimeException){
        if(tiaojian){
            throw runtimeException;
        }
    }
    public static void throwIf(boolean tiaojian,ErrorCode errorCode){
        throwIf(tiaojian,new BusinessException(errorCode));
    }
    public static void throwIf(boolean tiaojian,ErrorCode errorCode,String message){
        throwIf(tiaojian,new BusinessException(errorCode,message));
    }


}
