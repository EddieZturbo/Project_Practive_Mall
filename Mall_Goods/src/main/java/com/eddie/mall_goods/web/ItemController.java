package com.eddie.mall_goods.web;

import com.eddie.mall_goods.service.SkuInfoService;
import com.eddie.mall_goods.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 @author EddieZhang
 @create 2023-02-07 7:53 PM
 */
@Controller
public class ItemController {
    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 根据skuId展示对应商品的详情页面
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String itemPage(@PathVariable("skuId") Long skuId, Model model){
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item",skuItemVo);
        return "item";
    }
}
