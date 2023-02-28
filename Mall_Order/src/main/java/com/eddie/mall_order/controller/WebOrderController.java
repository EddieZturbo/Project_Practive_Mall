package com.eddie.mall_order.controller;

import com.eddie.mall_order.service.OrderService;
import com.eddie.mall_order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.ExecutionException;

/**
 @author EddieZhang
 @create 2023-02-27 4:13 PM
 */
@Controller
public class WebOrderController {
    @Autowired
    OrderService orderService;

    /**
     * 去结算确认页面
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.orderConfirm();
        model.addAttribute("orderConfirmData",orderConfirmVo);
        return "confirm";
    }
}
