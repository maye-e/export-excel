package com.may.service;

import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public interface ExportService extends IService<LinkedHashMap> {

    //sql查询
    List<LinkedHashMap> customQuery(String sql);
    //方法重载，自定义分页查询
    IPage<LinkedHashMap> customQuery(String sql, IPage<LinkedHashMap> page);

    //获取总页数
    Integer getPages(String sql, IPage<LinkedHashMap> page);

    /**
     * excel 查询任务
     * @param pair sql语句
     * @return
     * @throws Exception
     */
    Callable<Pair<ResultSet, ExcelWriter>> buildTask(Pair<String, ExcelWriter> pair);

    // excel 导出
    void exportExcel(Pair<ResultSet,ExcelWriter> pair) throws SQLException;

    /**
     * 一个sql的多个分页任务
     * @param sql 原始sql
     * @param pages 页数
     * @param fileNameTemplate 导出的文件名模板。若只有一页，则输出文件名就是模板名，否则，自动在模板名后加 -n
     */
    void oneSqlOfPagesTask(String sql, Integer pages, String fileNameTemplate);

    /**
     * 遍历所有的sql文件，然后提交查询任务
     */
    void loopSqlFilesAndSubmit();

    /**
     * 获取线程返回值，然后导出excel
     */
    void getFutureAndExport() throws InterruptedException, ExecutionException,SQLException;

    //主工作
    void doWork() throws Exception;
}
