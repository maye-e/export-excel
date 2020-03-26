package com.may.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "export")
@NoArgsConstructor
public class ExportConfig {
    private Integer pageSize;//分页大小
    private String sqlDirectory;//sql 所在的目录
    private String excelDirectory;//导出 excel 的目录
}
