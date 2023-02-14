package com.eddie.mall_thirdparty.controller;

import com.eddie.common.utils.R;
import com.eddie.mall_thirdparty.utils.SendSms;
import org.springframework.web.bind.annotation.*;

/**
 @author EddieZhang
 @create 2023-02-14 11:18 PM
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @RequestMapping("/sendCode")
    @ResponseBody
    public R sendCode(@RequestBody String phone){
        SendSms.send(phone);
        return R.ok();
    }
}
