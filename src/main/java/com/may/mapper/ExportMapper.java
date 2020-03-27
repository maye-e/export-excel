package com.may.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.LinkedHashMap;
import java.util.List;

@Mapper
public interface ExportMapper extends BaseMapper<LinkedHashMap> {
    List<LinkedHashMap> customQuery(String sql);
    List<LinkedHashMap> customQuery(String sql, IPage<LinkedHashMap> page);

    // 注解方式 sql
    @Select("${sql}")
    List<LinkedHashMap> customQuery2(String sql);
    @Select( "${sql}")
    List<LinkedHashMap> customQuery2(String sql,IPage<LinkedHashMap> page);
}
