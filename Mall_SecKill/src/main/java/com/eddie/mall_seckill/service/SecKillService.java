package com.eddie.mall_seckill.service;

import com.eddie.mall_seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 @author EddieZhang
 @create 2023-03-17 9:19 AM
 */
public interface SecKillService {
    /**
     * 定期扫描最近三天需要进行秒杀的商品并进行上架
     */
    void uploadSeckillSkuLatest3Days();

    /**
     * 获取当前参与秒杀的商品信息
     * @return
     */
    List<SeckillSkuRedisTo> getCurrentSecKillSkus();

    /**
     * 根据skuId查询当前商品是否参加秒杀活动
     * @param skuId
     * @return
     */
    SeckillSkuRedisTo getSecKillInfoBySkuId(Long skuId);

    /**
     * 秒杀商品
     * @param killId
     * @param key
     * @param num
     * @return
     */
    String kill(String killId, String key, Integer num);
}
