package com.may.aspectj;

import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Console;
import com.may.annotation.MethodTimeInterval;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 定义一个切面，计算方法执行的时间
 */
@Component
@Aspect
public class MethodTimeIntervalAspect {
    private TimeInterval interval = new TimeInterval();

    private String methodDescription;

    // 配置织入点
    @Pointcut("@annotation(com.may.annotation.MethodTimeInterval)")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void doBefore(JoinPoint joinPoint) {
        // 获得注解
        MethodTimeInterval methodTimeInterval = getAnnotationLog(joinPoint);
        if (methodTimeInterval == null) return;
        methodDescription = methodTimeInterval.value();//获得注解值
        Console.log("[{}] 开始...",methodDescription);
        interval.restart();//开始计时
    }

    @After("pointCut()")
    public void doAfter(){

        Console.log("[{}] 完成，耗时：[{}]",methodDescription,interval.intervalPretty());
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private MethodTimeInterval getAnnotationLog(JoinPoint joinPoint)
    {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method == null) return null;
        return method.getAnnotation(MethodTimeInterval.class);
    }
}
