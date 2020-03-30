package com.may.test;

import cn.hutool.core.bean.DynaBean;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.may.ApplicationTest;
import com.may.controller.ExportExcel;
import com.may.entity.TBTest;
import com.may.mapper.ExportMapper;
import com.may.mapper.TBTestMapper;
import com.may.service.ExportService;
import com.may.utils.PageWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={ApplicationTest.class})// 指定启动类
public class MyTest {

    @Resource
    private ExportService exportService;

    @Resource
    private ExportMapper exportMapper;

    @Resource
    private TBTestMapper tbTestMapper;

    @Resource
    private ExportExcel exportExcel;

    @Test
    public void test(){
        int[] count = NumberUtil.range(1, 10);
        int[] cols = NumberUtil.range(1, 30);
        List<TBTest> list = new ArrayList<>();
        //创建动态 bean. DynaBean.create() 还有一个重载方法,亦可传入 bean.class. 但直接传类时,会报异常,提示没有bean中对应的方法
        Arrays.stream(count).forEach(i -> {
            DynaBean tbTest = DynaBean.create(new TBTest());
            Arrays.stream(cols).forEach(j -> tbTest.set("clo" + j, RandomUtil.randomEle(getDataArray())));//这里用动态bean通过反射调用set方法
            list.add(tbTest.getBean());
        });
        Console.log(list);
//        tbTestService.saveBatch(list);
    }

    @Test
    public void test2(){
        Page<TBTest> page = new Page<>(1,2);
        IPage<TBTest> tbTestIPage = tbTestMapper.selectPage(page, new QueryWrapper<TBTest>());
        Console.log(tbTestIPage.getRecords().size());

    }

    @Test
    public void test3() throws Exception{
        exportService.get();
    }

    @Test
    public void test4(){
        IPage page = exportService.customQuery("select * from tb_test", new Page<>(1, 2));
        List<LinkedHashMap> records = page.getRecords();
        System.out.println("总记录数："+page.getTotal());
        System.out.println("总页数"+page.getPages());
        records.stream().map(el -> el.get("id")).forEach(System.out::print);
    }

    @Test
    public void test5(){

        exportExcel.doWork();
    }



    public String[] getDataArray() {
        String[] array = {"你一定会很好 很好很好",
                "你会流连早起的晨曦",
                "你会永别痛苦，失望，离殇",
                "你会一直有年轻的志向",
                "愿你想去远方，就去远方",
                "愿你想回故乡，就回故乡",
                "你会有很多幸福作为补偿",
                "你不是最好的，但我只爱你",
                "童年是一杯咖啡，喝着让人回味无穷",
                "每个人都是幸福的。只是，你的幸福，常常在别人眼里"};
        return array;
    }
}
