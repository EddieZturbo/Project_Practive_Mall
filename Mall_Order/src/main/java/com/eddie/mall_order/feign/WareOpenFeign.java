package com.eddie.mall_order.feign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 @author EddieZhang
 @create 2023-02-28 9:59 PM
 */
@FeignClient("cloud-mall-ware")
public interface WareOpenFeign {
    /**
     * 根据skuId查看是否有库存
     * @param skuIds
     * @return
     */
    @PostMapping("/mall_ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
}
