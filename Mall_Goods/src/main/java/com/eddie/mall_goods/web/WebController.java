package com.eddie.mall_goods.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 @author EddieZhang
 @create 2023-01-28 12:32 PM
 */
@Controller
public class WebController {
    @RequestMapping("/")
    public String indexPage(){
        return "index";
    }
}
