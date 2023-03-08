package com.eddie.mall_ware.feign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 @author EddieZhang
 @create 2023-03-08 10:14 PM
 */
@FeignClient("cloud-mall-order")
public interface OrderOpenFeign {
    @GetMapping(value = "/mall_order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
