package com.may.aspectj;

import cn.hutool.core.lang.Console;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GlobalExceptionHandler {

    @Pointcut("execution(* com.may.*..*(..))")
    public void pointCut() {
    }

    /*
    @Before  在切点方法之前执行  before(JoinPoint joinPoint)
    @After  在切点方法之后执行
    @AfterReturning 切点方法返回后执行  doAfterReturningAdvice1(JoinPoint joinPoint,Object result)
    @AfterThrowing 切点方法抛异常执行 doAfterThrowingAdvice(JoinPoint joinPoint,Throwable exception)
    @Around 属于环绕增强，能控制切点执行前，执行后，用这个注解后，程序抛异常，会影响@AfterThrowing这个注解，不要同时使用 doAroundAdvice(ProceedingJoinPoint proceedingJoinPoint)
     */
    @Around("pointCut()")
    public Object handlerException(ProceedingJoinPoint proceedingJoinPoint) {
        try {
            return proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            Console.error("全局异常处理====[{}]",e.getMessage());
        }
        return null;
    }
}
