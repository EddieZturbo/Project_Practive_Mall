package com.eddie.mall_authserver.feign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 @author EddieZhang
 @create 2023-02-14 11:26 PM
 */
@FeignClient("cloud-mall-Third-Party")
@Component
public interface ThirdSmsOpenFeign {
    @RequestMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestBody String phone);
}
