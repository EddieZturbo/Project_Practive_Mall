package com.eddie.mall_seckill.service;

import com.eddie.mall_seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 @author EddieZhang
 @create 2023-03-17 9:19 AM
 */
public interface SecKillService {
    void uploadSeckillSkuLatest3Days();

    /**
     * 获取当前参与秒杀的商品信息
     * @return
     */
    List<SeckillSkuRedisTo> getCurrentSecKillSkus();
}
