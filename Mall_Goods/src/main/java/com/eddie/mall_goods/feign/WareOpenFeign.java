package com.eddie.mall_goods.feign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 @author EddieZhang
 @create 2023-01-26 4:32 PM
 */
@FeignClient("cloud-mall-ware")
public interface WareOpenFeign {
    /**
     * 远程调用cloud-mall-ware服务 根据skuId进行库存查询
     * @param skuIds
     * @return
     */
    @PostMapping("mall_ware/waresku/hasStock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds);
}
