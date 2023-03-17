package com.eddie.mall_seckill.feign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 @author EddieZhang
 @create 2023-03-17 9:22 AM
 */
@FeignClient("cloud-mall-coupon")
public interface CouponOpenFeign {
    /**
     * 远程调用coupon服务 获取最近三天的秒杀活动
     * @return
     */
    @GetMapping("/mall_coupon/seckillsession/Lates3DaySession")
    public R getLates3DaySession();
}
