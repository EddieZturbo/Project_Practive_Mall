package eddie.com.mall_cart.service;

import eddie.com.mall_cart.vo.CartItemVo;
import eddie.com.mall_cart.vo.CartVo;

import java.util.List;
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

    CartItemVo getCartItemBySkuId(Long skuId);

    CartVo getCart() throws ExecutionException, InterruptedException;

    void checkItem(Long skuId, Integer checked);

    CartItemVo getCartItem(Long skuId);

    void deleteItem(Long skuId);

    void countItem(Long skuId, Integer num);

    List<CartItemVo> getUserCartItems();
}
