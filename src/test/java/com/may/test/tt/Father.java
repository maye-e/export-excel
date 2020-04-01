package com.may.test.tt;

public class Father {

    public Father() {
        System.out.println("father create");
    }

    public boolean fun(String s){
        return fun(s,null);
    }

    public boolean fun(String s,Integer a){
        return true;
    }
}
