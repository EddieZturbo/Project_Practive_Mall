package eddie.com.mall_cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eddie.common.utils.R;
import eddie.com.mall_cart.feign.GoodsOpenFeign;
import eddie.com.mall_cart.interceptor.UserCartInterceptor;
import eddie.com.mall_cart.service.CartService;
import eddie.com.mall_cart.to.UserInfoTo;
import eddie.com.mall_cart.vo.CartItemVo;
import eddie.com.mall_cart.vo.CartVo;
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
import java.util.stream.Collectors;

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
                SkuInfoVo skuInfoVo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
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

            CompletableFuture.allOf(goodsInfoFuture, goodsSaleAttrFuture).get();//等待指定的异步线程完成后进行操作 get()方法会阻塞主线程直至指定的异步线程完成操作
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
     * 根据skuId到redis中查询购物车中指定的item数据
     * @param skuId
     * @return
     */
    @Override
    public CartItemVo getCartItemBySkuId(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String itemDataString = (String) cartOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(itemDataString, CartItemVo.class);
        return cartItemVo;
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

    /**
     * 获取登录以及临时用户时所有的购物车数据
     * @return
     */
    @Override
    public CartVo getCart() throws ExecutionException, InterruptedException {
        CartVo cartVo = new CartVo();//要进行封装的购物车数据对象

        //得到当前用户的信息 判断是临时用户还是登录用户
        UserInfoTo userInfoTo = UserCartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {//登录用户 获取登录用户的购物车数据（若有临时数据则需要将临时数据合并到登录用户的购物车中）
            String tempUserKey = CART_PREFIX + userInfoTo.getUserKey();//临时用户在redis中绑定的key
            String loginUserKey = CART_PREFIX + userInfoTo.getUserId();//登录用户在redis中绑定的key
            //判断是否有临时用户的购物车数据 若有则合并到登录用户的购物车中
            List<CartItemVo> cartItems = getCartItems(tempUserKey);
            if(null != cartItems && cartItems.size() > 0){
                for (CartItemVo item ://遍历获取到的临时用户的购物车的所有购物项 合并到登录用户的购物车中
                        cartItems) {
                    this.addToCart(item.getSkuId(), item.getCount());
                }
                //合并临时购物车后清空临时购物车的数据
                redisTemplate.delete(tempUserKey);
            }
            //查询出登录用户所绑定的redis中的购物车数据
            List<CartItemVo> loginUserCartItems = getCartItems(loginUserKey);
            cartVo.setItems(loginUserCartItems);
        } else {//临时用户 获取临时用户的购物车数据
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();//临时用户在redis中绑定的key
            List<CartItemVo> cartItems = getCartItems(cartKey);//根据临时用户的key获取redis中的购物车数据项
            cartVo.setItems(cartItems);//将获取到的所有的购物车的数据线赋值到要返回给前端的CartVo对象
        }
        return cartVo;
    }
    /**
     * 根据key（登录用户或者临时用户）获取redis中储存的购物车所有的数据项
     * @param cartKey
     * @return
     */
    private List<CartItemVo> getCartItems(String cartKey) {
        //获取临时用户绑定的key的购物车中所有的商品
        BoundHashOperations ops = redisTemplate.boundHashOps(cartKey);//绑定了临时用户的key的redis操作
        List<Object> values = ops.values();//临时用户redis中储存的购物车数据项的集合
        if (values.size() > 0 && values != null) {
            List<CartItemVo> cartItemVoStream = values.stream().map((item) -> {
                String itemString = item.toString();//将redis中的数据转成String方便JSON解析为指定的Java对象
                CartItemVo cartItem = JSON.parseObject(itemString, CartItemVo.class);//解析为指定的Java对象
                return cartItem;
            }).collect(Collectors.toList());
            return cartItemVoStream;
        }
        return null;
    }

    /**
     * 检查购物车商品项是否被勾选上
     * @param skuId
     * @param checked
     */
    @Override
    public void checkItem(Long skuId, Integer checked) {
        //查看购物车中的商品
        CartItemVo cartItem = getCartItem(skuId);
        //修改商品的状态
        cartItem.setCheck(checked == 1 ? true : false);
        //将修改好状态的对象的序列化String储存到redis中
        String jsonString = JSON.toJSONString(cartItem);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();//获取redis的hash操作
        cartOps.put(skuId.toString(),jsonString);
    }

    /**
     * 查看购物车中的商品
     * @param skuId
     * @return
     */
    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();//获取redis的hash操作
        String cartItem = (String) cartOps.get(skuId.toString());//根据skuId获取购物车商品的String
        CartItemVo cartItemVo = JSON.parseObject(cartItem, CartItemVo.class);
        return cartItemVo;
    }

    /**
     * 删除购物车中的购物项
     * @param skuId
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 修改购物车中购物项的数量
     * @param skuId
     * @param num
     */
    @Override
    public void countItem(Long skuId, Integer num) {
        //根据skuId查询购物车中的商品项
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        //将修改好数量的商品项对象的序列化String储存到redis中
        String jsonString = JSON.toJSONString(cartItem);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),jsonString);
    }


}
