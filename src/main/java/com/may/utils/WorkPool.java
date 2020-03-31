package com.may.utils;

import cn.hutool.core.thread.ThreadUtil;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 工作窃取模式的线程池
 */
public class WorkPool {

    // sqlSession 并行查询的数量. 这里只设置为2,多了怕数据库受不了
    private static ExecutorService pool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

    public static ExecutorService get(){
        CompletionService<Object> completionService = ThreadUtil.newCompletionService();
        
        return pool;
    }
}
