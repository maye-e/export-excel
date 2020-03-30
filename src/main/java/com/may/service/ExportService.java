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

public interface ExportService extends IService<LinkedHashMap> {

    //sql查询
    List<LinkedHashMap> customQuery(String sql);
    //方法重载，自定义分页查询
    IPage<LinkedHashMap> customQuery(String sql, IPage<LinkedHashMap> page);

    //获取总页数
    Integer getPages(String sql, IPage<LinkedHashMap> page);

    /**
     * excel 查询线程
     * @param pair sql语句
     * @return
     * @throws Exception
     */
    Callable<Pair<ResultSet, ExcelWriter>> queryForExcelTask(Pair<String, ExcelWriter> pair);

    // excel 导出
    void exportExcel(Pair<ResultSet,ExcelWriter> pair) throws SQLException;
}
