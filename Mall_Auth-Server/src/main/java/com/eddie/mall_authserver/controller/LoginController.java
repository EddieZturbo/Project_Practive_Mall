package com.eddie.mall_authserver.controller;

import com.eddie.common.constant.AuthServerConstant;
import com.eddie.common.exception.BizCodeEnum;
import com.eddie.common.utils.R;
import com.eddie.mall_authserver.feign.ThirdSmsOpenFeign;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;

/**
 @author EddieZhang
 @create 2023-02-14 10:44 PM
 */
@Controller
public class LoginController {
    @Autowired
    ThirdSmsOpenFeign thirdSmsOpenFeign;

    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone){

        //TODO 解决接口防刷

        String value = (String) redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(StringUtils.isNotEmpty(value)){//若redis中已经存在验证码了
            //验证码获取后的60s内不能再次获取
            if((System.currentTimeMillis() - Long.parseLong(value.split("_")[1])) <= 60000L){
                return R.error(
                        BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),
                        BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        //验证码再次校验（使用redis）
        redisTemplate.opsForValue().set(
                AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,
                "1234" + "_" + System.currentTimeMillis(),//在redis中缓存验证码的值的时候顺便配上系统时间 防止60s内再次有请求进来发送验证码
                10,
                TimeUnit.MINUTES);
        return thirdSmsOpenFeign.sendCode(phone);
    }



}
