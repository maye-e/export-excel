package com.may.domain;

import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.may.config.ExportConfig;
import com.may.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 批量导出查询 mysql 中的数据到 excel
 * 目前没做多线程，因为会提示内存超过 gc 限制的错误
 */
@Slf4j
@Controller
public class ExportExcel {

    @Resource
    private ExportConfig config;

    @Resource
    private ExportService exportService;

    public void main(String[] args) {
        String sqlDirectory = config.getSqlDirectory();
        if (!StrUtil.endWith(sqlDirectory, "\\")) {
            config.setSqlDirectory(sqlDirectory + "\\");//补全目录，下面好操作
        }
    }

    public void doWork() {
        TimeInterval interval = new TimeInterval();
        interval.start();

        List<File> fileList = getSqlFileList(config.getSqlDirectory());
        fileList.stream().forEach(file -> {



//            exportService.customQuery("select * from tb_test");


            //文件名
            String fileName = StrUtil.removeSuffix(file.getName(), ".sql");

        });


        /*for (File file : fileList) {


            for (int i : ArrayUtil.range(0, totalPage)) {
                    int offset = i * pageSize;
                    String pageSql = StrUtil.format("{} limit {},{};", StrUtil.sub(finalSql, 0, -1), offset, pageSize);
                    Connection conn = null;
                    try {
                        conn = ds.getConnection();
                        PreparedStatement statement = conn.prepareStatement(pageSql);
                        ResultSet result = statement.executeQuery();
                        ResultSetMetaData metaData = result.getMetaData();
                        int columnCount = metaData.getColumnCount();// 列数

                        String outFile = totalPage > 1 ? fileName + "-" + (i + 1) : fileName;
                        // 这里实际是创建了一个 SXSSFBook
                        ExcelWriter writer = ExcelUtil.getBigWriter(1000);// 内存中的行数为 1000
                        writer.setDestFile(new File(StrUtil.format("{}{}.xlsx", filePath, outFile)));
                        writer.disableDefaultStyle();//禁用默认样式
                        boolean hasWriteHead = false;// 控制写入头
                        while (result.next()) {
                            HashMap<String, Object> map = new LinkedHashMap<>(columnCount);
                            // ArrayUtil.range 含头不含尾 0-30
                            for (int j : ArrayUtil.range(0, columnCount)) {
                                // 索引从 1 开始
                                String columnLabel = metaData.getColumnLabel(j + 1);
                                map.put(columnLabel, result.getObject(columnLabel));
                            }
                            if (!hasWriteHead) {
                                writer.writeHeadRow(map.keySet());
                                hasWriteHead = true;
                            }
                            writer.writeRow(map.values());//只是将数据写入 sheet
                        }
                        writer.flush();// 将数据刷如磁盘，刷新后会关闭流
                        writer.close();
                        log.error("导出成功：{}", outFile);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        DbUtil.close(conn);
                    }
            }
        }*/

//        log.error("全部导出完成，共耗时：{}", interval.intervalPretty());
    }



    /**
     * 格式化 sql 字符串
     * @param file sql文件
     * @return 格式化后的 sql 字符串
     */
    private String getSqlStr(File file){
        String sql = FileUtil.readString(file, "utf-8");
        return StrUtil.endWith(sql, ";") ? sql : sql + ";";//补全分号，字符串操作好统一
    }

    /**
     * 获取指定目录下的sql文件
     * @param sqlDirectory
     * @return 获取到的 sql 文件
     */
    private List<File> getSqlFileList(String sqlDirectory){
        // 文件过滤器,只要目标文件
        FileFilter fileFilter = file -> FileUtil.pathEndsWith(file, "sql") ? true : false;
        List<File> fileList = FileUtil.loopFiles(sqlDirectory, fileFilter);
        log.info("------已读取 sql 文件数：{} ------", fileList.size());
        return fileList;
    }

    /**
     * 自动调整合适的分页，每个excel中的行数平均（这样不太好，导出的 excel 会多出一些）
     *
     * @param cnt sql查询结果的总行数
     * @return 分页大小
     */
    private Integer autoPageSize(int cnt) {
        // 设定分页大小的限制是不超过 60w
        int pow = 0;
        while (true) {
            // 假如600000.001，这里向下取整（舍弃）即可，在计算页数那会向上取整
            int size = cnt / (int) Math.pow(2, pow);
            if (size <= 600000) {
                return size;
            }
            pow += 1;
        }
    }
}