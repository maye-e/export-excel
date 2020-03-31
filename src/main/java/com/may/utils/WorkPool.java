package com.may.utils;

import java.util.concurrent.CountDownLatch;

public class WorkPool {

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(10);
        while (latch.getCount() > 0){
            System.out.println(latch.getCount());
            latch.countDown();
        }
    }
}
