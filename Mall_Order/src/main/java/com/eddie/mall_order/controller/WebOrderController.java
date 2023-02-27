package com.eddie.mall_order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 @author EddieZhang
 @create 2023-02-27 4:13 PM
 */
@Controller
public class WebOrderController {
    /**
     * 去结算
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(){
        return "confirm";
    }
}
