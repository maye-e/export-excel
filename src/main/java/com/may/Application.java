package com.may;

import cn.hutool.aop.ProxyUtil;
import com.may.aop.TimeIntervalAspect;
import com.may.controller.ExportExcel;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.may.mapper")
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        ExportExcel exportExcel = (ExportExcel)context.getBean("exportExcel");
        ExportExcel excel = ProxyUtil.proxy(exportExcel, TimeIntervalAspect.class);
        excel.aopDemo();
    }

}
