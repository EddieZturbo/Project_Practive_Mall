package com.eddie.mall_search.feign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 @author EddieZhang
 @create 2023-02-07 12:21 PM
 */
@Component
@FeignClient("cloud-mall-goods")
public interface GoodsOpenFeign {
    @RequestMapping("mall_goods/attr/info/{attrId}")
    //@RequiresPermissions("mall_goods:attr:info")
    public R info(@PathVariable("attrId") Long attrId);
}
