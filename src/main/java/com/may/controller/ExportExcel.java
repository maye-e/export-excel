package com.may.controller;

import cn.hutool.core.util.StrUtil;
import com.may.annotation.MethodTimeInterval;
import com.may.config.ExportConfig;
import com.may.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Slf4j
@Controller
public class ExportExcel {

    @Resource
    private ExportConfig config;

    @Resource
    private ExportService exportService;

    public void main0(String[] args) {
        String sqlDirectory = config.getSqlDirectory();
        if (!StrUtil.endWith(sqlDirectory, "\\")) {
            config.setSqlDirectory(sqlDirectory + "\\");//补全目录，下面好操作
        }
    }

    @MethodTimeInterval("主程序操作")
    public void doWork() throws Exception{
        exportService.doWork();
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