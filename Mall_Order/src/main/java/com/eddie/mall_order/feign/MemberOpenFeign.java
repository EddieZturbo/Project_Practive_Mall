package com.eddie.mall_order.feign;

import com.eddie.common.utils.R;
import com.eddie.mall_order.vo.FareVo;
import com.eddie.mall_order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 @author EddieZhang
 @create 2023-02-27 4:57 PM
 */
@FeignClient("cloud-mall-member")
public interface MemberOpenFeign {
    /**
     * 根据memberId获取收货地址列表
     * @param memberId
     * @return
     */
    @GetMapping("/mall_member/memberreceiveaddress/{memberId}/address")
    List<MemberAddressVo> addressEntityList(@PathVariable("memberId") Long memberId);

    /**
     * 根据收货地址计算运费
     * @param addrId
     * @return
     */
    @GetMapping("/mall_member/memberreceiveaddress/deliveryFare")
    R deliveryFare(@RequestParam("addrId") Long addrId);
}
