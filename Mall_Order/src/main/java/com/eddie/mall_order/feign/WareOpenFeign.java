package com.eddie.mall_order.feign;

import com.eddie.common.utils.R;
import com.eddie.mall_order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    /**
     * 查询运费和收货地址信息
     * @param addrId
     * @return
     */
    @GetMapping(value = "/mall_ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    /**
     * 锁定库存
     * @param vo
     * @return
     */
    @PostMapping("/mall_ware/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo);


}
