package eddie.com.mall_cart.controller;

import eddie.com.mall_cart.service.CartService;
import eddie.com.mall_cart.vo.CartItemVo;
import eddie.com.mall_cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String cart(Model model) throws ExecutionException, InterruptedException {
        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }

    /**
     * 添加到购物车请求 跳转至success添加成功页面
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);//根据skuId将商品添加到购物车

        //TODO redirectAttributes进行重定向时的参数传递(flash使用session一次性使用,addAttribute则自动将参数进行url拼接)
        redirectAttributes.addAttribute("skuId", skuId);

        return "redirect:http://cart.zhangjinhao.com/addCartSuccessPage";//使用重定向 防止刷新页面 导致添加购物车操作重复进行
    }

    /**
     * TODO 当添加商品到购物车成功后 重定向到的页面 根据skuId进行购物车信息查询操作 防止页面刷新时导致添加购物车操作重复进行
     * @param skuId
     * @return
     */
    @GetMapping("/addCartSuccessPage")
    public String addCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        CartItemVo cartItemVo = cartService.getCartItemBySkuId(skuId);
        model.addAttribute("cartItem", cartItemVo);
        return "success";
    }

    /**
     * 商品是否选中
     * @param skuId
     * @param checked
     * @return
     */
    @GetMapping(value = "/checkItem")
    public String checkItem(@RequestParam(value = "skuId") Long skuId,
                            @RequestParam(value = "checked") Integer checked) {

        cartService.checkItem(skuId, checked);

        return "redirect:http://cart.zhangjinhao.com/cartList";

    }

    /**
     * 删除购物车中的购物项
     * @param skuId
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam(value = "skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.zhangjinhao.com/cartList";
    }


    @GetMapping("/countItem")
    public String countItem(@RequestParam(value = "skuId") Long skuId,
                            @RequestParam(value = "num") Integer num) {
        cartService.countItem(skuId,num);
        return "redirect:http://cart.zhangjinhao.com/cartList";
    }
}
