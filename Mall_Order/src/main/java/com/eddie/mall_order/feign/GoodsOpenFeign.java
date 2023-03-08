package com.eddie.mall_order.feign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 @author EddieZhang
 @create 2023-03-08 4:02 PM
 */
@FeignClient("cloud-mall-goods")
public interface GoodsOpenFeign {
    @GetMapping("/mall_goods/spuinfo/skuId/{skuId}")
    public R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
