package com.cq.YunPhoto.Aop;

import com.cq.YunPhoto.Annotation.AuthCheck;
import com.cq.YunPhoto.Common.BaseResponse;
import com.cq.YunPhoto.Common.ResultUtils;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.enums.UserRoleEnum;
import com.cq.YunPhoto.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户权限拦截的切面
 */

@Component
@Aspect
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object authCheck(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        //获取当前方法权限
        String auth  = authCheck.value();
        UserRoleEnum byCode = UserRoleEnum.getByCode(auth);
        //获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User userInfo = userService.getLoginUser(request);
        //获取当前登录用户的权限
        String userRole = userInfo.getUserRole();
        UserRoleEnum userRoleEnum = UserRoleEnum.getByCode(userRole);
        //不需要权限，直接发行
        if(byCode == null) {
            return joinPoint.proceed();
        }
        //判断当前用户是否具有权限
        if(userRoleEnum == null) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        //判断当前方法是否为管理员权限，但用户无管理员权限
        if(byCode.equals(UserRoleEnum.ADMIN) && !userRoleEnum.equals(UserRoleEnum.ADMIN)) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        //如果权限都无问题，直接放行
        return joinPoint.proceed();

    }
}
