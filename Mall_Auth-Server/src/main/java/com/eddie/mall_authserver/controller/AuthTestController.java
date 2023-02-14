package com.eddie.mall_authserver.controller;

import com.eddie.common.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 @author EddieZhang
 @create 2023-02-14 8:03 PM
 */
@RestController
public class AuthTestController {
    @RequestMapping("/testAuthServer")
    public R testAuthServer(){
        return R.ok();
    }
}
