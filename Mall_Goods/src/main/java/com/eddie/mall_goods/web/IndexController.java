package com.eddie.mall_goods.web;

import com.eddie.mall_goods.entity.CategoryEntity;
import com.eddie.mall_goods.service.CategoryService;
import com.eddie.mall_goods.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 @author EddieZhang
 @create 2023-01-28 12:32 PM
 */
//TODO 整合Thymeleaf视图解析器 web静态页面的mapping
@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @RequestMapping({"/","/index.html"})
    public String indexPage(Model model){
        //获取一级分类菜单
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1Categories();
        model.addAttribute("categories",categoryEntityList);
        return "index";
    }

    //index/json/catalog.json
    @GetMapping(value = "/index/catalog.json")
    @ResponseBody
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();

        return catalogJson;

    }

}
