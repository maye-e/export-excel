package com.may.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.may.entity.TBTest;
import com.may.mapper.TBTestMapper;
import com.may.service.TBTestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TBTestServiceImpl extends ServiceImpl<TBTestMapper,TBTest> implements TBTestService {
}
