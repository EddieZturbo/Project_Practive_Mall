package com.eddie.mall_seckill.schedule;

import com.eddie.mall_seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 @author EddieZhang
 @create 2023-03-17 9:15 AM
 */
@Component
@Slf4j
public class SecKillSchedule {
    @Autowired
    SecKillService secKillService;
    @Autowired
    RedissonClient redissonClient;

    //秒杀商品上架功能的锁
    private static final String upload_lock = "seckill:upload:lock";

    /**
     * TODO 保证上架商品的幂等性
     * 定时扫描最近三天需要上架的商品进行上架
     */
    @Scheduled(cron = "0 1/1 * * * ? ")//每小时扫描一次
    public void uploadSeckillSkuLatest3Days(){
        log.info("自动上架商品。。。");
        //分布式锁
        RLock lock = redissonClient.getLock(upload_lock);//锁的name决定锁的粒度

        try {
            lock.lock(10, TimeUnit.SECONDS);//上锁(并指定锁自动释放时间)
            secKillService.uploadSeckillSkuLatest3Days();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();//下锁
        }
    }
}
