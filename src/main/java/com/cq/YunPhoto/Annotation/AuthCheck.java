package com.cq.YunPhoto.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 */
@Target(ElementType.METHOD)//表示该注解在方法上生效
@Retention(RetentionPolicy.RUNTIME)//表示该注解在运行时生效
public @interface AuthCheck {
    String value() default "";
}
