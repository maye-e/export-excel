import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;

import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) {
        Console.log(NumberUtil.range(1,14));
        TimeInterval interval = new TimeInterval();
        interval.start();
        try{TimeUnit.SECONDS.sleep(3);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println(interval.intervalPretty());
        interval.restart();
//        interval.intervalRestart();
        try{TimeUnit.SECONDS.sleep(2);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println(interval.intervalPretty());
        interval.intervalRestart();
        try{TimeUnit.SECONDS.sleep(4);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println(interval.intervalPretty());
    }
}
