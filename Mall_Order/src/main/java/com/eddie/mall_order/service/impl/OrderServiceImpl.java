package com.eddie.mall_order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.eddie.common.utils.R;
import com.eddie.common.vo.MemberResponseVo;
import com.eddie.mall_order.feign.CartOpenFeign;
import com.eddie.mall_order.feign.MemberOpenFeign;
import com.eddie.mall_order.feign.WareOpenFeign;
import com.eddie.mall_order.interceptor.UserOrderInterceptor;
import com.eddie.mall_order.vo.MemberAddressVo;
import com.eddie.mall_order.vo.OrderConfirmVo;
import com.eddie.mall_order.vo.OrderItemVo;
import com.eddie.mall_order.vo.SkuStockVo;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_order.dao.OrderDao;
import com.eddie.mall_order.entity.OrderEntity;
import com.eddie.mall_order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberOpenFeign memberOpenFeign;

    @Autowired
    CartOpenFeign cartOpenFeign;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    WareOpenFeign wareOpenFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = baseMapper.selectPage(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取确认订单页面所需要的数据
     * @return
     */
    @Override
    public OrderConfirmVo orderConfirm() throws ExecutionException, InterruptedException {
        //构建需要返回给前端的vo对象
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        //获取当前用户的信息
        MemberResponseVo memberResponseVo = UserOrderInterceptor.threadLocal.get();

        //TODO RequestContextHolder获取当前请求的header数据(返回当前绑定到线程的请求属性底层使用ThreadLocal)
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();


        //TODO 远程查询feign会代理的invoke一个新的请求 会丢失原本请求的header信息(将RequestContextHolder获取到的当前请求的header数据分别放置一份到异步线程中供使用)
        CompletableFuture<Void> memberAddressFuture = CompletableFuture.runAsync(() -> {
            //TODO 异步调用另起线程需要将RequestContextHolder获取到的当前请求的header数据分别放置一份到异步线程中供使用
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询所有的收获地址列表 并将查询到的地址列表封装到vo对象对象中
            List<MemberAddressVo> memberAddressVos = memberOpenFeign.addressEntityList(memberResponseVo.getId());
            orderConfirmVo.setMemberAddressVos(memberAddressVos);
        }, threadPoolExecutor);


        CompletableFuture<Void> currentCartItemFuture = CompletableFuture.runAsync(() -> {
            //TODO 异步调用另起线程需要将RequestContextHolder获取到的当前请求的header数据分别放置一份到异步线程中供使用
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询购物车所有选中的购物项 并封装到vo对象对象中
            List<OrderItemVo> currentCartItem = cartOpenFeign.getCurrentCartItem();
            orderConfirmVo.setItems(currentCartItem);
        }, threadPoolExecutor).thenRunAsync(() -> {//currentCartItemFuture异步执行后
            List<OrderItemVo> orderConfirmVoItems = orderConfirmVo.getItems();//currentCartItemFuture异步执行后封装好的订单购物项的数据
            //获取订单所有购物项的skuId
            List<Long> allItemsSkuId = orderConfirmVoItems.stream()
                    .map(item -> {
                        return item.getSkuId();
                    })
                    .collect(Collectors.toList());
            //远程调用ware服务根据skuId查看商品的库存情况
            R hasStock = wareOpenFeign.getSkuHasStock(allItemsSkuId);
            List<SkuStockVo> hasStockData = hasStock.getData("data", new TypeReference<List<SkuStockVo>>() {});
            if(null != hasStockData && hasStockData.size() > 0){//判断是否获取到数据项的库存数据
                //将获取到的库存数据封装成map<skuId,hasStock> 并封装到OrderConfirmVo对象中返回给前端使用
                Map<Long, Boolean> hasStockBySkuIdMap = hasStockData.stream()
                        .collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(hasStockBySkuIdMap);
            }
        },threadPoolExecutor);


        //用户积分
        Integer integration = memberResponseVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        //TODO 防重令牌

        //当异步任务完成后执行后续操作
        CompletableFuture.allOf(memberAddressFuture,currentCartItemFuture).get();

        return orderConfirmVo;
    }

}