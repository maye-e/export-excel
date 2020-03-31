package com.may.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.may.mapper.ExportMapper;
import com.may.service.ExportService;
import org.apache.commons.lang3.tuple.Pair;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExportServiceImpl extends ServiceImpl<ExportMapper, LinkedHashMap> implements ExportService {

    @Resource
    private ExportMapper exportMapper;

    @Resource
    private SqlSessionTemplate sqlSession;//springboot自动注入了 sqlsession,无需手动配置

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
    public Callable<Pair<ResultSet, ExcelWriter>> queryForExcelTask(Pair<String, ExcelWriter> pair) {
        return () -> {
            Connection conn = sqlSession.getConnection();
            PreparedStatement statement = conn.prepareStatement(pair.getLeft());
            ResultSet result = statement.executeQuery();
            return Pair.of(result,pair.getRight());
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
}
