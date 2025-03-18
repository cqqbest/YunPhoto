package com.cq.YunPhoto.Exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.cq.YunPhoto.Common.BaseResponse;
import com.cq.YunPhoto.Common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 自动全局异常捕获器(用于捕获controller中抛出的异常并响应给前端)
 */

@RestControllerAdvice//捕获全局异常
@Slf4j
public class GlobalExceptionHandler {
    //捕获自定义异常
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException businessException){
        //向日志抛出异常
        log.error("BusinessException",businessException);
        //利用封装好的返回工具类向前端返回异常信息
        return ResultUtils.error(businessException.getCode(),businessException.getMessage());
    }
    //捕获系统运行时异常
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException runtimeException){
        //向系统抛出异常
        log.error("RuntimeException",runtimeException);
        //利用封装好的返回工具类向前端返回异常信息，统一返回为系统内部异常
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }

    /**
     * 捕获Sa-Token异常
     */
    //未登录异常
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginExceptionHandler(NotLoginException notLoginException){
        //向日志抛出异常
        log.error("NotLoginException",notLoginException);
        //利用封装好的返回工具类向前端返回异常信息
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);
    }
    //未授权异常
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException notPermissionException){
        //向日志抛出异常
        log.error("NotPermissionException",notPermissionException);
        //利用封装好的返回工具类向前端返回异常信息
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
    }
}
