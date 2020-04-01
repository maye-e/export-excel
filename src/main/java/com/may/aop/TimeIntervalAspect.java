package com.may.aop;

import cn.hutool.aop.aspects.SimpleAspect;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Console;

import java.lang.reflect.Method;

public class TimeIntervalAspect extends SimpleAspect {

    private TimeInterval interval = new TimeInterval();

    @Override
    public boolean before(Object target, Method method, Object[] args) {
        Console.log("程序开始执行...");
        interval.start();
        return true;
    }


    /*
        这个方法也可以不用重写。但是，如果要重写这个方法，就必须重写下面这个方法，才会有用。
        因为 SimpleAspect 实现了 Aspect，而 Aspect 中没有3个参数的 before 方法。
     */
    @Override
    public boolean after(Object target, Method method, Object[] args) {
        return this.after(target,method,args,null);
    }

    @Override
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        Console.log("程序执行结束，耗时：[{}]", interval.intervalPretty());
        return true;
    }
}
