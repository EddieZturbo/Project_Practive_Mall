package eddie.com.mall_cart.controller;

import eddie.com.mall_cart.interceptor.UserCartInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 @author EddieZhang
 @create 2023-02-17 1:37 PM
 */
@Controller
public class CartController {

    @GetMapping("/cartList")
    public String cart(){
        System.out.println(Thread.currentThread().getName() + "\tThreadLocal中的数据:" + UserCartInterceptor.threadLocal.get());
        return "cartList";
    }
}
