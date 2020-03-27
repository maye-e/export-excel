package com.may.controller;

import cn.hutool.core.bean.DynaBean;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import com.may.entity.TBTest;
import com.may.service.TBTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 总共预备导入 100w 数据，服务器是 2核 4G，内存小一次不能插太多
 */
@Slf4j
@Controller
public class InsertDataThread {

    @Resource
    private TBTestService tbTestService;

    private static Integer POLL_SIZE = 3;//线程池大小
    private static Integer DATA_ROWS = 1000000;//总共要插入的数据行数，100W
    private static Integer DATALIST_SIZE = 1000;//默认生成的数据集大小为1000，而saveBatch() 默认插入的大小也是1000

    private static volatile AtomicInteger SUCCESS_COUNT = new AtomicInteger(0);//插入成功线程数
    private static volatile AtomicInteger FAIL_COUNT = new AtomicInteger(0);//插入失败线程数

    /**
     *工作方法
     */
    public void doWork() {
        TimeInterval interval = new TimeInterval();
        interval.start();//开始计时

        // 线程池
        ExecutorService pool = ThreadUtil.newExecutor(POLL_SIZE);
        //每生成 dataListSize 条数据，调用线程插入一次，因此总共有 dataRows/dataListSize 个线程要操作
        int processThread = DATA_ROWS / DATALIST_SIZE;
        CountDownLatch latch = ThreadUtil.newCountDownLatch(processThread);

        IntStream.range(0,processThread).forEach(i -> {
            List<TBTest> dataList = getDataList(DATALIST_SIZE);
            pool.execute(() -> {
                if (insertDataList(dataList, null)){
                    log.info("第 {}/{} 个线程插入完成",SUCCESS_COUNT.incrementAndGet(),processThread);
                }else {
                    log.error("第 {} 个线程插入失败！！！",FAIL_COUNT.incrementAndGet());
                }
                latch.countDown();//线程执行完，计时器减一
            });
        });
        //线程等待
        try {latch.await();} catch (InterruptedException e) {e.printStackTrace();}
        //关闭线程池
        pool.shutdown();
        log.info("------------ 数据插入完毕!!!成功线程数：{} ,失败线程数：{} ,耗时：{} ------------", SUCCESS_COUNT.get(),FAIL_COUNT.get(),interval.intervalPretty());
    }

    /**
     * 批量向数据库插入测试数据
     * @param tbTestList
     */
    private Boolean insertDataList(List<TBTest> tbTestList,Integer batchSize) {
        return tbTestService.saveBatch(tbTestList,batchSize);
    }
    //方法重载
    private Boolean insertDataList(List<TBTest> tbTestList) {
        return tbTestService.saveBatch(tbTestList);
    }

    /**
     * 向数据库插入一条数据
     * @param tbTest
     * @return
     */
    private Boolean insertData(TBTest tbTest){
        return tbTestService.save(tbTest);
    }

    /**
     * 获取测试用的数据样例
     * @return 随机一条数据
     */
    public String getRandomData() {
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
        return RandomUtil.randomEle(array);
    }

    /**
     * 获得一条待插入的数据
     * @return
     */
    private TBTest getData() {
        //字段数
        //创建动态 bean.    DynaBean.create() 还有一个重载方法,亦可传入 bean.class. 但直接传类时,会报异常,提示没有bean中对应的方法
        DynaBean tbTest = DynaBean.create(new TBTest());
        //这里用动态bean通过反射调用set方法
        IntStream.rangeClosed(1,30).forEach(i -> tbTest.set("clo" + i, getRandomData()));// rangeClosed 区间：[1,30]
        return tbTest.getBean();
    }

    /**
     * 获得多条待插入数据
     * @param rows 需要得到的数据行数
     * @return 数据集合
     */
    private List<TBTest> getDataList(int rows){
        List<TBTest> tbTestList = new ArrayList<>();
        IntStream.range(0,rows).forEach(i ->tbTestList.add(getData()));// rang 区间：[0,row)
        return tbTestList;
    }
}
