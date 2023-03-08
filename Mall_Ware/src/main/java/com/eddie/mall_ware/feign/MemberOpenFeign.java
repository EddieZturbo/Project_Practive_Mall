package com.eddie.mall_ware.feign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 @author EddieZhang
 @create 2023-03-08 3:18 PM
 */
@FeignClient("cloud-mall-member")
public interface MemberOpenFeign {
    /**
     * 根据id获取用户地址信息
     * @param id
     * @return
     */
    @RequestMapping("/mall_member/memberreceiveaddress/info/{id}")
    public R info(@PathVariable("id") Long id);
}
