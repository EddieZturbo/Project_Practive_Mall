package eddie.com.mall_cart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import eddie.com.mall_cart.vo.CartItemVo;

import java.util.concurrent.ExecutionException;

/**
 @author EddieZhang
 @create 2023-02-16 9:25 PM
 */
public interface CartService {

    /**
     * 添加商品到购物车方法
     * @param skuId
     * @param num
     * @return
     */
    CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;
}
