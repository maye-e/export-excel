package com.may.service;

import com.may.entity.TBTest;

import java.util.List;

public interface TBTestService {

    int insert(TBTest tbTest);
    int insertBatch(List<TBTest> tbTestList);
}
