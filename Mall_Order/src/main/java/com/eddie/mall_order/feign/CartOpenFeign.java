package com.eddie.mall_order.feign;

import com.eddie.mall_order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 @author EddieZhang
 @create 2023-02-28 10:25 AM
 */
@FeignClient("cloud-cart-server")
public interface CartOpenFeign {
    /**
     * 查询当前用户购物车中的所有选中的数据项
     * @return
     */
    @RequestMapping("/currentUserCartItems")
    @ResponseBody
    List<OrderItemVo> getCurrentCartItem();
}
