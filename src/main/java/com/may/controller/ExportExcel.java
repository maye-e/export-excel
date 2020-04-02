package com.may.controller;

import cn.hutool.core.util.StrUtil;
import com.may.annotation.MethodTimeInterval;
import com.may.config.ExportConfig;
import com.may.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

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

    public void main0(String[] args) {
        String sqlDirectory = config.getSqlDirectory();
        if (!StrUtil.endWith(sqlDirectory, "\\")) {
            config.setSqlDirectory(sqlDirectory + "\\");//补全目录，下面好操作
        }
    }


    public void doWork() throws Exception{
        exportService.doWork();
        //todo TimeInterval aop
    }


    @MethodTimeInterval("aopDemo")
    public void aopDemo(){
        System.out.println("我要睡2秒...");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("我睡醒了！");
    }

    public static void main(String[] args) {
        new ExportExcel().aopDemo();
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