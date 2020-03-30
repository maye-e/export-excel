package com.may.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 工作窃取模式的线程池
 */
public class WorkStealingPool {

    // sqlSession 并行查询的数量. 这里只设置为2,多了怕数据库受不了
    private static ExecutorService pool = Executors.newWorkStealingPool(2);

    public static ExecutorService get(){
        return pool;
    }
}
