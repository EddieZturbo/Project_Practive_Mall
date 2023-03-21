package com.eddie.mall_order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.constant.CartConstant;
import com.eddie.common.exception.NoStockException;
import com.eddie.common.to.OrderTo;
import com.eddie.common.to.mq.SeckillOrderTo;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;
import com.eddie.common.utils.R;
import com.eddie.common.vo.MemberResponseVo;
import com.eddie.mall_order.constant.OrderConstant;
import com.eddie.mall_order.dao.OrderDao;
import com.eddie.mall_order.entity.OrderEntity;
import com.eddie.mall_order.entity.OrderItemEntity;
import com.eddie.mall_order.enume.OrderStatusEnum;
import com.eddie.mall_order.feign.CartOpenFeign;
import com.eddie.mall_order.feign.GoodsOpenFeign;
import com.eddie.mall_order.feign.MemberOpenFeign;
import com.eddie.mall_order.feign.WareOpenFeign;
import com.eddie.mall_order.interceptor.UserOrderInterceptor;
import com.eddie.mall_order.service.OrderItemService;
import com.eddie.mall_order.service.OrderService;
import com.eddie.mall_order.to.OrderCreateTo;
import com.eddie.mall_order.to.SpuInfoVo;
import com.eddie.mall_order.vo.*;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.eddie.common.constant.CartConstant.CART_PREFIX;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();//TODO 将OrderSubmitVo对象放置ThreadLocal中供线程中使用

    @Autowired
    MemberOpenFeign memberOpenFeign;

    @Autowired
    CartOpenFeign cartOpenFeign;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    WareOpenFeign wareOpenFeign;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    GoodsOpenFeign goodsOpenFeign;

    @Autowired
    RabbitTemplate rabbitTemplate;

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

        //TODO RequestContextHolder获取当前请求的header数据[解决Feign异步调用丢失请求头问题](返回当前绑定到线程的请求属性底层使用ThreadLocal)
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
            List<SkuStockVo> hasStockData = hasStock.getData("data", new TypeReference<List<SkuStockVo>>() {
            });
            if (null != hasStockData && hasStockData.size() > 0) {//判断是否获取到数据项的库存数据
                //将获取到的库存数据封装成map<skuId,hasStock> 并封装到OrderConfirmVo对象中返回给前端使用
                Map<Long, Boolean> hasStockBySkuIdMap = hasStockData.stream()
                        .collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(hasStockBySkuIdMap);
            }
        }, threadPoolExecutor);


        //用户积分
        Integer integration = memberResponseVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        //价格数据自动计算

        //TODO 防重令牌(幂等性)
        //为用户设置一个token保存到redis中 并设置过期时间
        String token = UUID.randomUUID().toString().replace("-", "");//生成随机token
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        //当异步任务完成后执行后续操作
        CompletableFuture.allOf(memberAddressFuture, currentCartItemFuture).get();

        return orderConfirmVo;
    }

    /**
     * 提交订单的方法
     *      提交订单要处理的事项
     *          * 1.验token（确保提交订单的幂等性）
     *          * 2.下订单（创建订单）
     *          * 3.验价
     *          * 4.锁定库存
     * @param orderSubmitVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)//TODO 采用本地事务 + RabbitMQ的组合形式达到分布式事务的BASE目标（追高并发 保证最终一致性）
    // @GlobalTransactional(rollbackFor = Exception.class)为了保证高并发，'不推荐使用seata'，因为是加锁，并行化，提升不了效率,可以发消息给库存服务
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        //将OrderSubmitVo对象放置ThreadLocal中供线程中使用
        orderSubmitVoThreadLocal.set(orderSubmitVo);
        //准备提交订单要返回的对象
        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        submitOrderResponseVo.setCode(0);//设置状态信息

        //获取当前订单用户对象vo(当前用户的信息)
        MemberResponseVo memberResponseVo = UserOrderInterceptor.threadLocal.get();

        //TODO 验证令牌[令牌验证同时删除令牌的操作确保原子性（lua脚本）]
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = orderSubmitVo.getOrderToken();//前端带来的订单token 需要与redis中的进行比较
        Long result = (Long) redisTemplate.execute(
                new DefaultRedisScript<Long>(luaScript, Long.class),/* script – must not be null.resultType – can be null. */
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()),//redis中储存的token
                orderToken);//前端传来的token
        if (result.equals(0L)) {//1.令牌验证
            //令牌验证失败
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        } else {
            //令牌验证成功
            //2.下订单（创建订单）
            OrderCreateTo order = createOrder();
            //3.验价
            BigDecimal payAmount = order.getOrder().getPayAmount();//根据数据库确认的价格
            BigDecimal payPrice = orderSubmitVo.getPayPrice();//前端传来的价格
            //金额比对
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {//Math的.abs(absolute)求绝对值方法 两个价格进行subtract若差距小于0.01则验价成功
                //验价成功
                //保存订单
                saveOrder(order);

                //4、库存锁定,只要有异常，回滚订单数据
                WareSkuLockVo lockVo = new WareSkuLockVo();//锁库存单号的对象
                lockVo.setOrderSn(order.getOrder().getOrderSn());

                //获取出要锁定的商品数据信息(从订单中获取商品项)
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(orderItemVos);//将需要锁定的商品项的数据赋值到锁库存单号的对象中

                //TODO 调用远程锁定库存的方法（这里使用RabbitMQ 追高并发 保证最终一致性）
                //出现的问题：扣减库存成功了，但是由于网络原因超时，出现异常，导致订单事务回滚，库存事务不回滚(解决方案：seata)
                //TODO 为了保证高并发，'不推荐使用seata'，因为是加锁，并行化，提升不了效率,可以发消息给库存服务
                R r = wareOpenFeign.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    //锁库成功
                    submitOrderResponseVo.setOrder(order.getOrder());
                    //订单提交成功
                    //TODO 发送消息给mq
                    //convertAndSend(this.exchange, routingKey, object, (CorrelationData) null);
                    //TODO order.create.order先发送给了延时队列(延时30min 若30分钟后closeOrder的消费者拿到了此延时消息并判断该未成功支付则自动进行关单操作)
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    //删除购物车中的数据
                    System.out.println("redis中购物车的key==>" + CartConstant.CART_PREFIX + memberResponseVo.getId());
                    BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(CartConstant.CART_PREFIX + memberResponseVo.getId());
                    List<OrderItemEntity> orderItems = order.getOrderItems();
                    List<Long> skuIds = orderItems.stream().map((item) -> {
                        return item.getSkuId();
                    }).collect(Collectors.toList());
                    boundHashOperations.delete(skuIds);
                    return submitOrderResponseVo;
                } else {
                    //锁定失败
                    String msg = (String) r.get("msg");
                    //获取锁定失败的原因并手动抛出为锁库的异常
                    throw new NoStockException(msg);
                }
            } else {
                //验价失败
                submitOrderResponseVo.setCode(2);
                return submitOrderResponseVo;
            }
        }
    }

    /**
     * 关单方法
     * @param orderEntity
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        //关闭订单之前先查询一下数据库，判断此订单状态是否已支付
        OrderEntity orderInfo = this.getOne(
                new LambdaQueryWrapper<OrderEntity>().eq(
                        OrderEntity::getOrderSn
                        , orderEntity.getOrderSn()));
        if(orderInfo.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())){
            //待付款状态 要进行关闭订单操作
            OrderEntity orderUpdate = new OrderEntity();
            orderUpdate.setId(orderInfo.getId());
            orderUpdate.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(orderUpdate);

            //发送消息给mq进行关单操作
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderInfo, orderTo);
            try {
                //TODO 确保每个消息发送成功，给每个消息做好日志记录，(给数据库保存每一个详细信息)保存每个消息的详细信息
                rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);
            } catch (AmqpException e) {
                //TODO 定期扫描数据库，重新发送失败的消息
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity orderEntity = this.baseMapper.selectOne(
                new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderSn, orderSn));
        return orderEntity;
    }

    /**
     * 生成秒杀订单
     * @param orderTo
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {
        //①保存订单信息
        //准备对单对象
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setMemberId(orderTo.getMemberId());
        orderEntity.setCreateTime(new Date());
        BigDecimal totalPrice = orderTo.getSeckillPrice().multiply(new BigDecimal(orderTo.getNum().toString()));//计算总价 sku数量multiply单价
        orderEntity.setPayAmount(totalPrice);
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());//设置订单状态为新建状态

        //保存订单对象到数据库中
        this.save(orderEntity);

        //②保存订单项信息
        //准备订单项对象
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(orderTo.getOrderSn());
        orderItem.setRealAmount(totalPrice);
        orderItem.setSkuQuantity(orderTo.getNum());
        //远程调用goods服务获取spu信息并封装到订单项对象中
        R spuInfoBySkuId = goodsOpenFeign.getSpuInfoBySkuId(orderTo.getSkuId());
        SpuInfoVo spuInfoVo = spuInfoBySkuId.getData("data", new TypeReference<SpuInfoVo>() {});
        orderItem.setSpuId(spuInfoVo.getId());
        orderItem.setSpuName(spuInfoVo.getSpuName());
        orderItem.setSpuBrand(spuInfoVo.getBrandName());
        orderItem.setCategoryId(spuInfoVo.getCatalogId());

        //保存订单项数据到数据库中
        orderItemService.save(orderItem);
    }

    /**
     * 生成订单方法
     * @return
     */
    private OrderCreateTo createOrder() {
        //准备方法要返回的TO对象
        OrderCreateTo createTo = new OrderCreateTo();
        //生成订单号(使用IdWorker.getTimeId()生成订单号)
        String orderSn = IdWorker.getTimeId();
        //构建订单
        OrderEntity orderEntity = builderOrder(orderSn);
        //获取到所有的订单项
        List<OrderItemEntity> orderItemEntities = builderOrderItems(orderSn);
        //验价(计算价格、积分等信息)
        computePrice(orderEntity, orderItemEntities);

        createTo.setOrder(orderEntity);
        createTo.setOrderItems(orderItemEntities);

        return createTo;
    }

    /**
     * 验价的方法
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        //总价
        BigDecimal total = new BigDecimal("0.0");
        //优惠价
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;
        //订单总额，叠加每一个订单项的总额信息
        for (OrderItemEntity orderItem : orderItemEntities) {
            //优惠价格信息
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            integration = integration.add(orderItem.getIntegrationAmount());

            //总价
            total = total.add(orderItem.getRealAmount());

            //积分信息和成长值信息
            integrationTotal += orderItem.getGiftIntegration();
            growthTotal += orderItem.getGiftGrowth();
        }
        //1、订单价格相关的
        orderEntity.setTotalAmount(total);
        //设置应付总额(总额+运费)
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);

        //设置积分成长值信息
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);
    }

    /**
     * 构建所有订单项数据
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> builderOrderItems(String orderSn) {
        //准备方法返回的对象
        List<OrderItemEntity> orderItemEntityList = new ArrayList<>();
        //查询当前用户购物车选中的商品项（确保购物项中的数据是实时的）
        List<OrderItemVo> currentCartItems = cartOpenFeign.getCurrentCartItem();
        if (null != currentCartItems && currentCartItems.size() > 0) {
            orderItemEntityList = currentCartItems.stream().map((items) -> {
                //构建订单项数据
                OrderItemEntity orderItemEntity = builderOrderItem(items);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        return orderItemEntityList;
    }

    /**
     * 构建订单项数据
     * @param items
     * @return
     */
    private OrderItemEntity builderOrderItem(OrderItemVo items) {
        //准备方法要返回的数据对象
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //1、商品的spu信息
        Long skuId = items.getSkuId();
        //远程调用goods服务 根据skuId获取对应spu的数据
        R spuInfoBySkuId = goodsOpenFeign.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoVo = spuInfoBySkuId.getData("data", new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandName());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());

        //2、商品的sku信息
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(items.getTitle());
        orderItemEntity.setSkuPic(items.getImage());
        orderItemEntity.setSkuPrice(items.getPrice());
        orderItemEntity.setSkuQuantity(items.getCount());

        //使用StringUtils.collectionToDelimitedString将list集合转换为String
        String skuAttrValues = StringUtils.collectionToDelimitedString(items.getSkuAttrValues(), ";");//使用;分隔
        orderItemEntity.setSkuAttrsVals(skuAttrValues);
        //3、商品的优惠信息

        //4、商品的积分信息
        orderItemEntity.setGiftGrowth(items.getPrice().multiply(new BigDecimal(items.getCount())).intValue());
        orderItemEntity.setGiftIntegration(items.getPrice().multiply(new BigDecimal(items.getCount())).intValue());
        //5、订单项的价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);
        //当前订单项的实际金额.总额 - 各种优惠价格
        //原来的价格{使用sku的单价}multiply乘数量
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        //原价减去优惠价得到最终的价格
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }

    /**
     * 构建订单方法
     * @param orderSn
     * @return
     */
    private OrderEntity builderOrder(String orderSn) {
        //获取当前用户登录信息
        MemberResponseVo memberResponseVo = UserOrderInterceptor.threadLocal.get();

        //准备方法要返回的对象
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(memberResponseVo.getId());
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberUsername(memberResponseVo.getUsername());

        //从TreadLocal中获取前端传来的OrderSubmitVo对象
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();

        //远程获取收货地址和运费信息
        R fareAddressVo = wareOpenFeign.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fareAddressVo.getData("data", new TypeReference<FareVo>() {
        });

        //获取到运费信息
        BigDecimal fare = fareResp.getFare();
        orderEntity.setFreightAmount(fare);

        //获取到收货地址信息
        MemberAddressVo address = fareResp.getAddress();
        //设置收货人信息
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());

        //设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);//自动确认时间（天）
        orderEntity.setConfirmStatus(0);//确认收货状态[0->未确认；1->已确认]
        return orderEntity;
    }


    /**
     * 保存订单到数据库方法
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        //获取订单信息
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        orderEntity.setCreateTime(new Date());
        //保存订单(将订单插入到数据库中)
        this.baseMapper.insert(orderEntity);

        //获取订单项信息
        List<OrderItemEntity> orderItems = order.getOrderItems();
        //批量保存订单项数据
        orderItemService.saveBatch(orderItems);
    }

}