package com.may.domain;

import cn.hutool.core.bean.DynaBean;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import com.may.entity.TBTest;
import com.may.service.TBTestService;
import com.may.service.impl.TBTestServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class InsertThread {
    /*
        总共预备导入 100w 数据，每 1w 数据开线程插入一次，也就是 100 个线程
        服务器是 2核 4G，内存小一次不能插太多
     */
    static final int THREAD_SUM = 10;
    static final int ROW_NUM = 10000;
    static volatile AtomicInteger AI = new AtomicInteger(0);
    static final DataSource ds = DSFactory.get("vm_db"); // 获取指定数据源
    static final String DB = "db_data";//要插入的库

    public static void main(String[] args) {
//        domain();
        int[] count = NumberUtil.range(1, 10);
        int[] cols = NumberUtil.range(1, 30);
        List<TBTest> list = new ArrayList<>();
        //创建动态 bean. DynaBean.create() 还有一个重载方法,亦可传入 bean.class. 但直接传类时,会报异常,提示没有bean中对应的方法
        Arrays.stream(count).forEach(i -> {
            DynaBean tbTest = DynaBean.create(new TBTest());
            Arrays.stream(cols).forEach(j -> tbTest.set("clo" + j, RandomUtil.randomEle(getDataArray())));//这里用动态bean通过反射调用set方法
            list.add(tbTest.getBean());
        });
        TBTestService tbTestService = new TBTestServiceImpl();
        tbTestService.insertBatch(list);

    }

    private static void domain() {
        // 计时器
        TimeInterval interval = new TimeInterval();
        // 线程池
        ExecutorService pool = ThreadUtil.newExecutor(5);
        CountDownLatch latch = ThreadUtil.newCountDownLatch(THREAD_SUM);

        int[] count = NumberUtil.range(1, THREAD_SUM);
        int[] rows = NumberUtil.range(1, ROW_NUM);
        int[] cols = NumberUtil.range(1, 30);
        interval.start();
        Arrays.stream(count).forEach(ctn -> pool.execute(() -> {
            List<Entity> dataList = new ArrayList<>(ROW_NUM);
            Arrays.stream(rows).forEach(i -> {
                Entity entity = Entity.create(DB);
                Arrays.stream(cols).forEach(j -> entity.set("clo" + j, RandomUtil.randomEle(getDataArray())));
                dataList.add(entity);
            });
            insertDatas(dataList);
            log.info("第 {}/{} 个线程插入完成",AI.incrementAndGet(),THREAD_SUM);
            latch.countDown();
        }));
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pool.shutdown();
        log.info("------------ 执行完毕，耗时：{} ------------", interval.intervalPretty());
    }


    /**
     * 获取测试用的数据样例
     *
     * @return
     */
    public static String[] getDataArray() {
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

    /**
     * @param entities
     */
    public static void insertDatas(List<Entity> entities) {
        try {
            Db.use(ds).insert(entities);
        } catch (SQLException e) {
            log.error("插入失败！", e);
        }
    }

}
