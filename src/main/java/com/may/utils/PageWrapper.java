package com.may.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public class PageWrapper {

    public static Page wrapper(List records, long current, long size){
        return new Page(current,size).setRecords(records);
    }

    public static Page wrapper(List records, long current, long size,boolean isSearchCount){
        return new Page(current,size,isSearchCount).setRecords(records);
    }

    public static Page wrapper(List records, Page page){
        return page.setRecords(records);
    }
}
