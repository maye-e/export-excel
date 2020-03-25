package com.may.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.may.entity.TBTest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TBTestMapper extends BaseMapper<TBTest> {

    int insertBatch(List<TBTest> tbTestList);// mybatis实现批量插入
}
