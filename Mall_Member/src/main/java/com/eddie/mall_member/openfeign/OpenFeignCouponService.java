package com.eddie.mall_member.openfeign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 @author EddieZhang
 @create 2022-12-16 23:17
 */
@Component
@FeignClient("cloud-mall-coupon")
public interface OpenFeignCouponService {

    @RequestMapping("/mall_coupon/coupon/getCouponByMember")
    public R getCouponByMember();

}
