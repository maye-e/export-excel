package com.may.service.impl;

import com.may.entity.TBTest;
import com.may.mapper.TBTestMapper;
import com.may.service.TBTestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TBTestServiceImpl implements TBTestService {

    @Resource
    private TBTestMapper tbTestMapper;

    @Override
    public int insert(TBTest tbTest) {
        return tbTestMapper.insert(tbTest);
    }

    @Override
    public int insertBatch(List<TBTest> tbTestList) {
        return tbTestMapper.insertBatch(tbTestList);
    }
}
