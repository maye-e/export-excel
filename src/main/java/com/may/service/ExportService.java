package com.may.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.LinkedHashMap;
import java.util.List;

public interface ExportService extends IService<LinkedHashMap> {

    //自定义sql查询
    List<LinkedHashMap<String,Object>> customQuery(String sql);

    //重载，分页查询
    IPage<LinkedHashMap> customQuery(String sql, Page<LinkedHashMap> page);

    //获取总页数
    Integer getPages(String sql, Page<LinkedHashMap> page);
}
