package com.eddie.mall_order.controller;

import com.eddie.common.exception.NoStockException;
import com.eddie.mall_order.service.OrderService;
import com.eddie.mall_order.vo.OrderConfirmVo;
import com.eddie.mall_order.vo.OrderSubmitVo;
import com.eddie.mall_order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
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

    /**
     * 提交订单
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes){
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(orderSubmitVo);
            if(responseVo.getCode().equals(0)){
                //成功
                model.addAttribute("submitOrderResp",responseVo);
                return "redirect:http://zhangjinhao.com";
            }else{
                //失败
                String msg = "下单失败";
                switch (responseVo.getCode()){
                    case 1: msg += "令牌订单信息过期，请刷新再次提交"; break;
                    case 2: msg += "订单商品价格发生变化，请确认后再次提交"; break;
                    case 3: msg += "库存锁定失败，商品库存不足"; break;
                }
                System.out.println(msg);
                redirectAttributes.addFlashAttribute("msg",msg);
                return "redirect:http://order.zhangjinhao.com/toTrade";
            }
        } catch (Exception e) {
            //一旦出现异常 判断是否是手动抛出的没库存的异常 返回给前端
            if (e instanceof NoStockException) {
                String message = e.getMessage();
                System.out.println(message);
                redirectAttributes.addFlashAttribute("msg",message);
            }
            System.out.println(e.getMessage());
            return "redirect:http://order.zhangjinhao.com/toTrade";
        }
    }
}
