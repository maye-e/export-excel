package com.may.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.may.mapper.ExportMapper;
import com.may.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExportServiceImpl extends ServiceImpl<ExportMapper, LinkedHashMap> implements ExportService {

    @Resource
    private ExportMapper exportMapper;

    @Override
    public List<LinkedHashMap<String, Object>> customQuery(String sql) {
        return exportMapper.customQuery(sql);
    }

    @Override
    public IPage<LinkedHashMap> customQuery(String sql, Page<LinkedHashMap> page) {
        List<LinkedHashMap> datas = exportMapper.customQuery(sql, page);
        return page.setRecords(datas);
    }
}
