package com.eddie.mall_goods.feign;

import com.eddie.common.utils.R;
import com.eddie.mall_goods.fallback.SecKillOpenFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 @author EddieZhang
 @create 2023-03-20 11:58 PM
 */
@FeignClient(value = "cloud-mall-seckill",fallback = SecKillOpenFeignFallback.class)
public interface SecKillOpenFeign {
    /**
     * 远程调用秒杀服务 根据skuId获取当前商品的秒杀详细信息
     * @param skuId
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSecKillInfoBySkuId(@PathVariable("skuId") Long skuId);
}
