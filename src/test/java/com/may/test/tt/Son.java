package com.may.test.tt;

import com.may.annotations.TimeInterval;

public class Son extends Father {

    public Son() {
        System.out.println("son create");
    }

    @TimeInterval("ss")
    @Override
    public boolean fun(String s){
        System.out.println("fun1"+s);
        return true;
    }


    @Override
    public boolean fun(String s,Integer a){
        System.out.println("fun2"+s+a);
        return true;
    }

    public static void main(String[] args) {
        Son son = new Son();
//        Encrypt encrypt = AnnotationUtils.findAnnotation(method, Encrypt.class);
        son.fun("may");
    }
}
