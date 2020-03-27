package com.may.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.LinkedHashMap;
import java.util.List;

public interface ExportService extends IService<LinkedHashMap> {

    //sql查询
    List<LinkedHashMap> customQuery(String sql);
    //方法重载，自定义分页查询
    IPage<LinkedHashMap> customQuery(String sql, IPage<LinkedHashMap> page);

    //获取总页数
    Integer getPages(String sql, IPage<LinkedHashMap> page);
}
