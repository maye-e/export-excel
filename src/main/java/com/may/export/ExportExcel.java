package com.may.export;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.setting.Setting;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileFilter;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 批量导出查询 mysql 中的数据到 excel
 * 目前做不了多线程，会提示内存超过 gc 限制的错误
 */
public class ExportExcel {

    static Setting setting = new Setting("config/config.setting");
    static final Log log = LogFactory.get("野哥温馨提示");
    static DataSource ds = DSFactory.get("vm_db"); // 获取指定数据源
    static Integer pageSize = setting.getInt("pageSize");
    static String filePath = setting.getStr("filePath");

    static {
        if (!StrUtil.endWith(filePath, "\\")) {
            filePath += "\\";//补全目录，下面好操作
        }
        if (pageSize == null) {
            pageSize = 600000;
        }
    }


    public static void main(String[] args) {
        TimeInterval interval = new TimeInterval();
        log.info("程序执行开始...");
        interval.start();
        ExportExcel exo = new ExportExcel();
        exo.domain();
        log.info("程序执行完毕,执行总时长: {}",interval.intervalPretty());
    }

    public void domain() {
        TimeInterval interval = new TimeInterval();
        interval.start();//启动一个计时器

        FileFilter fileFilter = file -> FileUtil.pathEndsWith(file, "sql") ? true : false;
        // 识别 classpath 下的文件，且兼容 spring风格，sql\\ 和 sql/ 都可以被识别
        List<File> files = FileUtil.loopFiles("sql",fileFilter);
        List<String> fileNames = files.stream().map(file -> file.getName()).collect(Collectors.toList());
        log.info("已读取 {} 个sql文件：{}", fileNames.size(), fileNames);
        if (fileNames.size() == 0) return;
        for (File file : files) {
            String readSql = FileUtil.readString(file, "utf-8");
            if (!StrUtil.endWith(readSql, ";")) {
                readSql += ";";//补全分号，下面字符串操作好统一
            }
            // 线程池 lambda 式需要 final 型变量
            String finalSql = readSql;
            Number totalCount = null;
            String sqlCount = StrUtil.format("select count(*) cnt {};", StrUtil.sub(finalSql, StrUtil.indexOfIgnoreCase(finalSql, "from"), -1));
            try {
                log.info("开始查询总行数...");
                totalCount = Db.use(ds).queryNumber(sqlCount);
                log.info("查询结束,耗时: {} ,总行数为：{}",interval.intervalPretty(), totalCount);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            int totalPage = (int) Math.ceil(totalCount.doubleValue() / pageSize);
            log.info("总共需要导出的excel个数为：{}", totalPage);
            //文件名
            String fileName = StrUtil.removeSuffix(file.getName(), ".sql");

            for (int i : NumberUtil.range(1, totalPage)) { // [1,totalPage]
                    int offset = (i-1) * pageSize;
                    String pageSql = StrUtil.format("{} limit {},{};", StrUtil.sub(finalSql, 0, -1), offset, pageSize);
                    Connection conn = null;
                    try {
                        conn = ds.getConnection();
                        PreparedStatement statement = conn.prepareStatement(pageSql);
                        log.info("开始进行第 {} 个查询...", i);
                        interval.restart();
                        ResultSet result = statement.executeQuery();
                        ResultSetMetaData metaData = result.getMetaData();
                        int columnCount = metaData.getColumnCount();// 列数

                        String outFile = totalPage > 1 ? fileName + "-" + i : fileName;
                        // 这里实际是创建了一个 SXSSFBook
                        ExcelWriter writer = ExcelUtil.getBigWriter(1000);// 内存中的行数为 1000
                        writer.setDestFile(new File(StrUtil.format("{}{}.xlsx", filePath, outFile)));
                        writer.disableDefaultStyle();//禁用默认样式
                        boolean hasWriteHead = false;// 控制写入头
                        log.info("第 {} 个查询完毕,耗时: {} ,开始导出第 {}/{} 个excel...", i,interval.intervalPretty(), i,totalPage);
                        interval.restart();
                        while (result.next()) {
                            HashMap<String, Object> map = new LinkedHashMap<>(columnCount);
                            // ArrayUtil.range 含头不含尾 0-30
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
                                    if (columnValue instanceof Timestamp || columnValue instanceof DateTime) {//包含日期和时间
                                        columnValue = DateUtil.formatDateTime(result.getTimestamp(columnLabel));//默认格式为：yyyy-MM-dd HH:mm:ss
                                    } else if (columnValue instanceof Time) {//只包含时间
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
                        writer.flush();// 将数据刷如磁盘，刷新后会关闭流
                        //writer.close();//close前会flush,二者选其一,不可都写,会报异常,提示流已关闭
                        Console.error("导出成功：{} ,耗时: {}", outFile,interval.intervalPretty());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        DbUtil.close(conn);
                    }
            }
        }
    }

    /**
     * 自动调整合适的分页，平均分配（这样不太好，导出的 excel 会多出一些）
     *
     * @param cnt sql查询结果的总行数
     * @return 分页大小
     */
    public Integer autoPageSize(int cnt) {
        // 设定分页大小的限制是不超过 60w
        int pow = 0;
        while (true) {
            // 假如600000.001，这里向下取整（舍弃）即可，在计算页数那会向上取整
            int size = cnt / (int) Math.pow(2, pow);
            if (size <= 600000) {
                return size;
            }
            pow += 1;
            continue;
        }

    }

    /**
     * 用 lamda 写法1
     */
    public static List<Entity> insertjdk81() {
        List<Entity> entities = new ArrayList<>(500);
        Stream.iterate(0, i -> i + 1).limit(entities.size()).forEach(i -> {
            Entity entity = Entity.create("user").set("name", "小丽" + i).set("age", "16");
            entities.add(entity);
            System.out.println(i);
        });
        return entities;
    }

    /**
     * 用 lamda 写法2
     */
    public static List<Entity> insertjdk82() {
        List<Entity> entities = new ArrayList<>(500);
        IntStream.range(0, entities.size()).forEach(i -> {
            Entity entity = Entity.create("user").set("name", "小华" + i).set("age", "16");
            entities.add(entity);
            System.out.println(i);
        });
        return entities;
    }
}
