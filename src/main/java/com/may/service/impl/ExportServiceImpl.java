package com.may.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.may.config.ExportConfig;
import com.may.mapper.ExportMapper;
import com.may.service.ExportService;
import com.may.utils.WorkTool;
import org.apache.commons.lang3.tuple.Pair;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExportServiceImpl extends ServiceImpl<ExportMapper, LinkedHashMap> implements ExportService {

    private static final ThreadLocal<Pair<String, ExcelWriter>> taskThreadLocal = new ThreadLocal<>();

    @Resource
    private ExportConfig config;

    @Resource
    private ExportMapper exportMapper;

    @Resource
    private SqlSessionTemplate sqlSession;//springboot自动注入了 sqlSession,无需手动配置

    @Override
    public List<LinkedHashMap> customQuery(String sql) {
        return exportMapper.customQuery(sql);
    }

    @Override
    public IPage<LinkedHashMap> customQuery(String sql, IPage<LinkedHashMap> page) {
        List<LinkedHashMap> dataList = exportMapper.customQuery(sql, page);
        return page.setRecords(dataList);
    }

    @Override
    public Integer getPages(String sql, IPage<LinkedHashMap> page) {
        return Convert.toInt(customQuery(sql,page).getPages());
    }

    @Override
    public Callable<Pair<ResultSet, ExcelWriter>> buildTask(Pair<String, ExcelWriter> pair) {
        taskThreadLocal.set(pair);
        return () -> {
            Pair<String, ExcelWriter> PairOf = taskThreadLocal.get();
            Connection conn = sqlSession.getConnection();
            PreparedStatement statement = conn.prepareStatement(PairOf.getLeft());
            ResultSet result = statement.executeQuery();
            return Pair.of(result,PairOf.getRight());
        };
    }

    @Override
    public void exportExcel(Pair<ResultSet, ExcelWriter> pair) throws SQLException {
        ResultSet result = pair.getLeft();
        ExcelWriter writer = pair.getRight();
        ResultSetMetaData metaData = result.getMetaData();
        int columnCount = metaData.getColumnCount();// 列数
        boolean hasWriteHead = false;// 控制写入头
        while (result.next()) {
            HashMap<String, Object> map = new LinkedHashMap<>(columnCount);
            for (int j : NumberUtil.range(1, columnCount)) {
                // 索引从 1 开始
                String columnLabel = metaData.getColumnLabel(j);
                Object columnValue = result.getObject(columnLabel);
                if (columnValue instanceof String) {
                    columnValue = result.getString(columnLabel);
                } else if (columnValue instanceof Integer) {
                    columnValue = result.getInt(columnLabel);
                } else if (columnValue instanceof Long) {
                    columnValue = Convert.toStr(result.getLong(columnLabel));//转换成字符串，避免在excel中显示成科学计数法
                } else if (columnValue instanceof Float) {
                    columnValue = result.getFloat(columnLabel);
                } else if (columnValue instanceof Double) {
                    columnValue = result.getDouble(columnLabel);
                } else if (columnValue instanceof BigDecimal) {
                    columnValue = result.getBigDecimal(columnLabel);
                } else if (columnValue instanceof Date) {//date 是父类，下面的都是它的子类
                    if (columnValue instanceof Timestamp) {//包含日期和时间
                        columnValue = DateUtil.formatDateTime(result.getTimestamp(columnLabel));//默认格式为：yyyy-MM-dd HH:mm:ss
                    }else if (columnValue instanceof DateTime){
                        columnValue = DateUtil.formatDateTime(result.getDate(columnLabel));
                    }else if (columnValue instanceof Time) {//只包含时间
                        columnValue = DateUtil.formatTime(result.getTime(columnLabel));
                    } else {//只包含日期
                        columnValue = DateUtil.formatDate(result.getDate(columnLabel));
                    }
                }
                map.put(columnLabel, columnValue);
            }
            if (!hasWriteHead) {
                writer.writeHeadRow(map.keySet());
                hasWriteHead = true;
            }
            writer.writeRow(map.values());//只是将数据写入 sheet
        }
        writer.flush();// 将数据刷入磁盘，刷新后会关闭流
    }

    @Override
    public void oneSqlOfPagesTask(String sql, Integer pages, String fileNameTemplate) {
        IntStream.rangeClosed(1,pages).forEach(i -> {
            int pageSize = config.getPageSize();
            int offset = (i - 1) * pageSize;
            String pageSql = StrUtil.format("{} limit {},{};", StrUtil.sub(sql, 0, -1), offset, pageSize);
            String excelFileName = fileNameTemplate;
            if (pages > 1){
                excelFileName += fileNameTemplate + i;
            }
            //输出文件路径
            String destFilePath = StrUtil.format("{}{}.xlsx",config.getExcelDirectory(),excelFileName);
            ExcelWriter writer = new BigExcelWriter(destFilePath);
            Pair<String, ExcelWriter> pair = Pair.of(pageSql, writer);
            Callable<Pair<ResultSet, ExcelWriter>> task = buildTask(pair);
            WorkTool.asyncExecTask(task);
        });
    }

    @Override
    public void loopSqlFilesAndSubmit() {
        List<File> fileList = WorkTool.loopSqlFiles(config.getSqlDirectory());
        fileList.stream().forEach(file -> {
            //一个sql文件中的语句
            String sql = WorkTool.getSqlStr(file);
            //将会导出到多少页
            Integer pages = getPages(sql, new Page<>(1,config.getPageSize()));
            //文件名模板。 最终输出的excel名会根据此模板创建
            String fileNameTemplate = StrUtil.removeSuffix(file.getName(), ".sql");
            //创建这条sql所有的分页导出任务，并提交
            oneSqlOfPagesTask(sql,pages,fileNameTemplate);
        });
    }

    @Override
    public void getFutureAndExport() throws InterruptedException,ExecutionException,SQLException{
        //已完成的任务数小于总任务数
        while (WorkTool.getCompletedTaskCount() < WorkTool.getTaskCount()){
            Future <Pair<ResultSet, ExcelWriter>> future = WorkTool.getFuture();
            Pair<ResultSet, ExcelWriter> pair = future.get();
            exportExcel(pair);
        }
    }


    @Override
    public void doWork() throws Exception{
        //1.遍历所有的sql文件，然后提交查询任务，等待返回结果
        loopSqlFilesAndSubmit();

        //2.取到线程执行的结果就去导出excel
        getFutureAndExport();
    }
}
