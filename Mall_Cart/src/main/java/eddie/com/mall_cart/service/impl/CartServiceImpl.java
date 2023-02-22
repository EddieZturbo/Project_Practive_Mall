package eddie.com.mall_cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eddie.common.utils.R;
import eddie.com.mall_cart.feign.GoodsOpenFeign;
import eddie.com.mall_cart.interceptor.UserCartInterceptor;
import eddie.com.mall_cart.service.CartService;
import eddie.com.mall_cart.to.UserInfoTo;
import eddie.com.mall_cart.vo.CartItemVo;
import eddie.com.mall_cart.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static com.eddie.common.constant.CartConstant.CART_PREFIX;

/**
 @author EddieZhang
 @create 2023-02-16 9:26 PM
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    GoodsOpenFeign goodsOpenFeign;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    /**
     * 添加商品到购物车方法
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        //首先查看redis中是否已经存在指定的skuId的数据
        // 若存在则进行数量上的添加即可 若不存在则远程调用商品服务根据skuId查找指定的商品信息并存储到redis中
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String goodsRedisValue = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(goodsRedisValue)) {
            CartItemVo cartItemVo = new CartItemVo();
            CompletableFuture<Void> goodsInfoFuture = CompletableFuture.runAsync(() -> {//异步编排 启动一个异步线程进行远程调用服务的操作
                //若redis中不存在就进行远程调用商品服务根据skuId查找指定的商品信息并存储到redis中
                R info = goodsOpenFeign.info(skuId);
                SkuInfoVo skuInfoVo = info.getData("attr", new TypeReference<SkuInfoVo>() {
                });
                cartItemVo.setSkuId(skuInfoVo.getSkuId());
                cartItemVo.setTitle(skuInfoVo.getSkuTitle());
                cartItemVo.setImage(skuInfoVo.getSkuDefaultImg());
                cartItemVo.setPrice(skuInfoVo.getPrice());
                cartItemVo.setCount(num);
            }, threadPoolExecutor);//指定线程池

            CompletableFuture<Void> goodsSaleAttrFuture = CompletableFuture.runAsync(() -> {//异步编排 启动一个异步线程进行远程调用服务的操作
                //远程调用商品服务查询skuAttrValues组合信息
                List<String> skuSaleAttrValues = goodsOpenFeign.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttrValues(skuSaleAttrValues);
            }, threadPoolExecutor);//指定线程池

            CompletableFuture.allOf(goodsInfoFuture,goodsSaleAttrFuture).get();//等待指定的异步线程完成后进行操作 get()方法会阻塞主线程直至指定的异步线程完成操作
            //将数据储存到redis中
            // 默认会使用jdk的序列化方式 要使用fastjson的序列化方式先将对象序列化为string再储存到redis中
            String s = JSON.toJSONString(cartItemVo);
            cartOps.put(skuId.toString(), s);
            return cartItemVo;

        } else {//购物车中不是空的修改数量即可
            //将redis中查询到的json字符串数据解析为Java对象
            CartItemVo cartItemVo = JSON.parseObject(goodsRedisValue, new TypeReference<CartItemVo>() {
            });
            //修改数量即可
            cartItemVo.setCount(cartItemVo.getCount() + num);
            //将修改好的数据写入到redis中
            String cartItemJson = JSON.toJSONString(cartItemVo);
            cartOps.put(skuId.toString(), cartItemJson);
            return cartItemVo;
        }
    }

    /**
     * redis中hashKey的绑定操作
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        //通过threadLocal判断当前用户的身份（登录用户/临时用户）
        UserInfoTo userInfo = UserCartInterceptor.threadLocal.get();
        String cartKey = "";
        if (null != userInfo.getUserId()) {//已经登录的用户
            cartKey = CART_PREFIX + userInfo.getUserId();
        } else {//临时登录用户
            cartKey = CART_PREFIX + userInfo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        return boundHashOperations;
    }
}
