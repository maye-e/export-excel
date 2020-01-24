package com.may.export;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.setting.Setting;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 多线程批量导出查询 mysql 中的数据到 excel
 */
public class ExportExcel_bak {

    static Setting setting = new Setting("config/config.setting");
    static final Log log = LogFactory.get("野哥温馨提示");
    static DataSource ds = DSFactory.get("vm_db"); // 获取指定数据源

    static Integer pageSize = setting.getInt("pageSize");
    static String filePath;
    static{
        filePath = setting.getStr("filePath");
        if (!StrUtil.endWith(filePath,"\\")){
            filePath += "\\";//补全目录，下面好操作
        }
        if (pageSize == null){
            pageSize = 600000;
        }
    }


    public static void main(String[] args) {
        ExportExcel_bak exo = new ExportExcel_bak();

        exo.domain();
    }

    public void domain() {
        TimeInterval interval = new TimeInterval();
        interval.start();

        // 将 classpth 下的文件路径转换为绝对路径，且兼容 spring风格，sql\\ 和 sql/ 都可以被识别
        List<File> files = FileUtil.loopFiles("sql");

        for (File file : files){
            // 将 classpth 下的文件路径转换为绝对路径，且兼容 spring风格，sql\\ 和 sql/ 都可以被识别
            String sql = FileUtil.readString(file, "utf-8");
            if (!StrUtil.endWith(sql,";")){
                sql += ";";//补全分号，下面字符串操作好统一
            }
            Number totalCount = null;
            String sqlCount = StrUtil.format("select count(*) cnt {};", StrUtil.sub(sql, StrUtil.indexOfIgnoreCase(sql, "from"), -1));
            try {
                totalCount = Db.use(ds).queryNumber(sqlCount);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            int totalPage = (int) Math.ceil(totalCount.doubleValue() / pageSize);

            //文件名
            String fileName = StrUtil.removeSuffix(file.getName(), ".sql");
            for (int i: ArrayUtil.range(0,totalPage)) {
                int offset = i * pageSize;
                StrUtil.endWith(sql,";");
                String pageSql = StrUtil.format("{} limit {},{};", StrUtil.sub(sql,0,-1), offset, pageSize);
                List<Entity> dataList = null;
                try {
                    dataList = Db.use(ds).query(pageSql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String outFile = totalPage > 1 ? fileName + "-" + (i+1) : fileName;

                BigExcelWriter writer = ExcelUtil.getBigWriter(StrUtil.format("{}{}.xlsx",filePath, outFile));
                List<List<Entity>> split = CollUtil.split(dataList, 1000);
                for (List<Entity> data : split){
                    writer.write(data, true);
                }
                writer.close();
                log.info("------已导出：{}", outFile);
            }
        }
        log.info("全部导出完成，共耗时：{}",interval.intervalPretty());
    }

    /**
     * 自动调整合适的分页，平均分配（这样不太好，导出的 excel 会多出一些）
     * @param cnt sql查询结果的总行数
     * @return 分页大小
     */
    public Integer autoPage(int cnt){
        // 设定分页大小的限制是不超过 60w
        int pow = 0;
        while (true){
            // 假如600000.001，这里向下取整（舍弃）即可，在计算页数那会向上取整
            int size = cnt/(int)Math.pow(2,pow);
            if(size <= 600000){
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