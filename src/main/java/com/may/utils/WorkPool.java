package com.may.utils;

import cn.hutool.core.lang.Console;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;

import java.util.*;
import java.util.concurrent.*;

/**
 * 工作窃取模式的线程池
 */
public class WorkPool {

    // sqlSession 并行查询的数量. 这里只设置为2,多了怕数据库受不了
    private static ExecutorService pool1 = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static ExecutorService pool = new ThreadPoolExecutor(1,//常驻线程数
            2,//最大线程数
            1L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10),//队列任务数
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy());
    private static   CompletionService<String> service = ThreadUtil.newCompletionService(pool1);

    private static CountDownLatch latch = new CountDownLatch(10);

    public static Callable<String> getTask(int i){
        Callable<String> task = () -> {
            Console.log("任务 {} 启动了...",i);
            Thread.sleep(5000);
            return "任务 " + i;
        };
        return task;
    }


    public static void main(String[] args) throws Exception {
        for (int i : ArrayUtil.range(10)){// [0,5)
            service.submit(getTask(i));
        }
        while (latch.getCount() != 0L){
            String s = service.take().get();
            System.out.println("取到："+s);
            latch.countDown();
        }
        latch.await();
       /* int j = 0;
        while (j < 10){

            Future<String> future = service.take();
            String s = future.get();
            j++;
            System.out.println("取到："+s);
        }*/
        pool1.shutdown();
    }
}
