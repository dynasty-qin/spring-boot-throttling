package com.example.throttling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author : Harry
 * Description : 开启ip访问限制
 * Date : 2020-08-20 16:49
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EnableIPLimit {

    /**
     * 指定时间内访问次数
     */
    long limitCount() default 10;

    /**
     * 单位时间(XX秒内允许访问XX次), 单位为秒
     */
    long time() default 60;

    /**
     * 超过限制后, 再次允许访问所需时间, 单位为分钟
     */
    long lockTime() default 60;
}
