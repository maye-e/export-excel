package com.may.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.mysql.cj.protocol.Resultset;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileFilter;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.*;

public class WorkTool {

    private static CompletionService service;
    private static ExecutorService pool;

    /**
     * 获取指定目录下的sql文件
     * @param filePath
     * @return 获取到的 sql 文件
     */
    public static List<File> loopSqlFiles(String filePath){
        // 文件过滤器,只要目标文件
        FileFilter fileFilter = file -> FileUtil.pathEndsWith(file, "sql") ? true : false;
        List<File> fileList = FileUtil.loopFiles(filePath, fileFilter);
        Console.log("------已读取 sql 文件数：{} ------", fileList.size());
        return fileList;
    }

    /**
     * 手动创建线程池
     * @return
     */
    private static final void createThreadPool(){
        pool = new ThreadPoolExecutor(1,//常驻线程数
                //因为sql语句大多都较为复杂，且结果集数量通常是几百万行。考虑到数据库压力，这里设置为2，让数据库并行2个查询
                2,//最大线程数
                1L,
                TimeUnit.SECONDS,
                /*工作中理应初始化任务队列数，否则这就是一个无边界队列，无限添加任务终将导致内存溢出。
                  不过，考虑本程序实际情况，任务数不可能会很多，但是数量不固定，根据sql的查询结果会有不同，
                  因此，就没有初始化队列任务数
                 */
                new LinkedBlockingQueue<>(),//队列任务数
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy());//直接丢弃任务，不处理也不抛异常
    }

    public static ExecutorService getThreadPool(){
        return pool;
    }

    /**
     * 返回活动的线程数
     * @return
     */
    public static int getActiveCount(){
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor)pool;
        return threadPool.getActiveCount();
    }

    /**
     * 返回任务数
     * @return
     */
    public static int getTaskCount(){
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor)pool;
        return (int)threadPool.getTaskCount();
    }

    /**
     * 返回已完成任务数
     * @return
     */
    public static int getCompletedTaskCount(){
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor)pool;
        return (int)threadPool.getCompletedTaskCount();
    }

    /**
     * 返回
     * @return
     * @throws InterruptedException
     */
    public static Future <Pair<ResultSet, ExcelWriter>> getFuture() throws InterruptedException{
        //poll方法不会阻塞，如果队列中没有成功的future，将返回 null
        return service.take();//如果队列中没有完成的线程，该方法会阻塞
    }

    /**
     * 创建一个任务执行器
     * @return
     */
    private static final void createService(){
        //ExecutorCompletionService 的队列中是已经执行完成的线程任务，默认也是 LinkedBlockingQueue，无边界队列
        //service = new ExecutorCompletionService<>(pool,new ArrayBlockingQueue<>(taskCount));// taskCount 任务数
        service = new ExecutorCompletionService<>(pool);
    }
    public static CompletionService getAsyncExecutor(){
        return service;
    }

    /**
     * 执行线程任务
     * @param task
     */
    public static void asyncExecTask(Callable task){
        service.submit(task);
    }

    /**
     * 格式化 sql 字符串
     * @param file sql文件
     * @return 格式化后的 sql 字符串
     */
    public static String getSqlStr(File file){
        String sql = FileUtil.readString(file, "utf-8");
        return StrUtil.endWith(sql, ";") ? StrUtil.sub(sql,0,-1) : sql;//去掉分号，避免查询分页报错
    }

}
