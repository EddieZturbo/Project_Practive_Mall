package com.eddie.mall_seckill.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 @author EddieZhang
 @create 2023-03-16 4:48 PM
 */
@Component
@Slf4j
public class ScheduleTaskTest {
    @Scheduled(cron = "* * * * * ?")//TODO 指定cron表达式定时执行该方法
    @Async//TODO 异步执行该方法
    public void scheduleTaskTest(){
        log.info("here is a task--->" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
