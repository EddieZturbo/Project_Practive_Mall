package com.eddie.mall_search.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 @author EddieZhang
 @create 2023-01-28 12:32 PM
 */
//TODO 整合Thymeleaf视图解析器 web静态页面的mapping
@Controller
public class SearchController {

    @RequestMapping({"/","/index.html"})
    public String indexPage(Model model){
        return "index";
    }

}
