package eddie.com.mall_cart.controller;

import eddie.com.mall_cart.interceptor.UserCartInterceptor;
import eddie.com.mall_cart.service.CartService;
import eddie.com.mall_cart.vo.CartItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.ExecutionException;

/**
 @author EddieZhang
 @create 2023-02-17 1:37 PM
 */
@Controller
public class CartController {
    @Autowired
    CartService cartService;

    @GetMapping("/cartList")
    public String cart(){
        System.out.println(Thread.currentThread().getName() + "\tThreadLocal中的数据:" + UserCartInterceptor.threadLocal.get());
        return "cartList";
    }

    /**
     * 添加到购物车请求 跳转至success添加成功页面
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num")  Integer num, Model model) throws ExecutionException, InterruptedException {
        //调用CartService服务的添加购物车方法并返回一个CartItemVo对象数据给到前端
        CartItemVo cartItemVo = cartService.addToCart(skuId,num);
        //将拿到的CartItemVo通过model的addAttribute方法返回到前端页面
        model.addAttribute("cartItem",cartItemVo);
        return "success";
    }
}
