package com.example.throttling.aspect;

import com.example.throttling.annotation.EnableIPLimit;
import com.example.throttling.util.IpUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Author : Harry
 * Description : 接口限流
 * Date : 2020-08-20 16:55
 */
@SuppressWarnings("ConstantConditions")
@Aspect
@Component
public class IPLimitAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Pointcut(value = "@annotation(com.example.throttling.annotation.EnableIPLimit)")
    public void pointcut() {}

    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        String prefix = "IP_LIMIT:";
        String lockPrefix = "IP_LOCK:";

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String requestURI = request.getRequestURI();
        if (StringUtils.isEmpty(requestURI)) {
            return joinPoint.proceed();
        }
        String realIP = IpUtils.getIpAddress(request);
        System.out.println(realIP);
        if (StringUtils.isEmpty(realIP)) {

            return joinPoint.proceed();
        }
        realIP = realIP.replaceAll(":", ".");
        String lockKey = lockPrefix + realIP;
        if (stringRedisTemplate.hasKey(lockKey)) {
            return "该IP被限制访问 !";
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        EnableIPLimit annotation = method.getAnnotation(EnableIPLimit.class);

        // 限制次数
        long limitCount = annotation.limitCount();
        // 锁定时间, 分钟
        long lockTime = annotation.lockTime();
        // 秒
        long time = annotation.time();

        String key = prefix + requestURI + ":" + realIP;
        if (stringRedisTemplate.hasKey(key)) {

            String s = stringRedisTemplate.opsForValue().get(key);
            if (Integer.valueOf(s) >= limitCount) {
                stringRedisTemplate.opsForValue().set(lockKey, s, lockTime, TimeUnit.MINUTES);
                return "该IP被限制访问 !";
            }
            stringRedisTemplate.opsForValue().increment(key);
        }else {
            stringRedisTemplate.opsForValue().set(key, "1", time, TimeUnit.SECONDS);
        }

        return joinPoint.proceed();
    }
}
