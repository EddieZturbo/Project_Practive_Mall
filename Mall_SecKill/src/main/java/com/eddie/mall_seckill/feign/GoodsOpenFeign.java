package com.eddie.mall_seckill.feign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 @author EddieZhang
 @create 2023-03-17 10:31 AM
 */
@FeignClient("cloud-mall-goods")
public interface GoodsOpenFeign {
    /**
     * 远程调用goods服务 根据skuId获取商品的详细信息
     * @param skuId
     * @return
     */
    @RequestMapping("/mall_goods/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
