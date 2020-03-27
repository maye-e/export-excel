package com.may.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.may.mapper.ExportMapper;
import com.may.service.ExportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.print.attribute.standard.PagesPerMinute;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExportServiceImpl extends ServiceImpl<ExportMapper, LinkedHashMap> implements ExportService {

    @Resource
    private ExportMapper exportMapper;

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
}
