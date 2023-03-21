package com.eddie.mall_seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.eddie.common.to.mq.SeckillOrderTo;
import com.eddie.common.utils.R;
import com.eddie.common.vo.MemberResponseVo;
import com.eddie.mall_seckill.feign.CouponOpenFeign;
import com.eddie.mall_seckill.feign.GoodsOpenFeign;
import com.eddie.mall_seckill.inteceptor.SecKillInterceptor;
import com.eddie.mall_seckill.service.SecKillService;
import com.eddie.mall_seckill.to.SeckillSkuRedisTo;
import com.eddie.mall_seckill.vo.SeckillSessionWithSkusVo;
import com.eddie.mall_seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 @author EddieZhang
 @create 2023-03-17 9:19 AM
 */
@Service
@Slf4j
public class SecKillServiceImpl implements SecKillService {
    private static final String SECKILL_SESSION_PREFIX = "seckill:sessions:";//redis的key--秒杀活动的key
    private static final String SECKILL_SKUS_PREFIX = "seckill:skus:";//redis的key--秒杀活动参与秒杀的所有商品的key
    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:semaphore:";    //+商品随机码
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    CouponOpenFeign couponOpenFeign;
    @Autowired
    GoodsOpenFeign goodsOpenFeign;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 定期扫描最近三天需要进行秒杀的商品并进行上架
     */
    @Override
    public void uploadSeckillSkuLatest3Days() {
        //远程调用coupon服务 扫描最近三天需要参加秒杀的活动
        R lates3DaySession = couponOpenFeign.getLates3DaySession();
        if (lates3DaySession.getCode() == 0) {
            //远程调用成功
            //上架商品
            List<SeckillSessionWithSkusVo> seckillSessionWithSkusVos = lates3DaySession.getData("data", new TypeReference<List<SeckillSessionWithSkusVo>>() {
            });
            //缓存到redis中
            if (null != seckillSessionWithSkusVos && seckillSessionWithSkusVos.size() > 0) {
                //1）缓存活动信息(List数据类型进行储存)
                saveSessionInfos(seckillSessionWithSkusVos);
                //2）缓存活动相关联的商品信息(Hash数据类型进行储存)
                saveSessionSkuInfo(seckillSessionWithSkusVos);
            }
        }

    }

    /**
     * 获取当前参加秒杀的商品信息
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSecKillSkus() {
        //根据当前时间 判断哪些场次进行秒杀(判断当前系统时间戳是否在某个秒杀场次的时间范围中)
        long time = new Date().getTime();//获取当前的系统时间戳

        //从redis中获取所有的场次和关联的商品信息
        //获取seckill:sessions:开头的所有key
        Set<String> keys = redisTemplate.keys(SECKILL_SESSION_PREFIX + "*");
        if (keys != null && keys.size() > 0) {
            //遍历所有key(seckill:sessions:1679119200000-1678950000000)
            for (String key : keys) {
                //截取key 获取start时间和end时间
                String replace = key.replace(SECKILL_SESSION_PREFIX, "");//将seckill:sessions:替换成空串
                log.info("前缀去除后==>" + replace);
                //1679119200000-1678950000000
                String[] split = replace.split("-");//将key依据-进行划分
                long startTime = Long.parseLong(split[0]);//开始时间戳
                long endTime = Long.parseLong(split[1]);//结束时间戳

                //判断当前时间是否在秒杀场次的开始时间戳和结束时间戳之间
                if (startTime <= time && time <= endTime) {
                    //获取当前秒杀场次关联的所有商品
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_SKUS_PREFIX);
                    assert range != null;//断言
                    List<String> listValues = hashOps.multiGet(range);
                    if (listValues != null && listValues.size() > 0) {
                        //将redis中获取到的秒杀商品的详情封装为前端展示的to数据
                        List<SeckillSkuRedisTo> seckillSkuRedisTos = listValues.stream()
                                .map(item -> {
                                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
                                    return seckillSkuRedisTo;
                                })
                                .collect(Collectors.toList());
                        return seckillSkuRedisTos;
                    }
                    break;//结束本次for循环
                }
            }
        }
        return null;
    }

    /**
     * 根据skuId查询当前商品是否参加秒杀活动
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo getSecKillInfoBySkuId(Long skuId) {
        //Redis中获取到所有参与秒杀的商品
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SECKILL_SKUS_PREFIX);//hash值绑定操作
        Set<String> keys = ops.keys();//所有参与秒杀的商品在redis中储存的hash值的key
        //通过正则表达式进行匹配 根据前端传来的skuId通过正则表达式进行匹配判断此商品是否参与秒杀
        if (null != keys && keys.size() > 0) {
            String reg = "\\d-" + skuId;//正则表达式
            for (String key :
                    keys) {
                if (Pattern.matches(reg, key)) {//判断当前商品是否能匹配（是否参与秒杀）
                    //匹配成功则从redis中获取秒杀商品的数据
                    String info = ops.get(key);
                    //将获取到的秒杀商品的信息通过JSON格式化转为指定的对象
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(info, SeckillSkuRedisTo.class);

                    //处理随机码（若当前处于秒杀时间才将随机码返回给前端 否则将随机码置为null）
                    long currentTimeMillis = System.currentTimeMillis();//系统当前时间戳
                    Long startTime = seckillSkuRedisTo.getStartTime();//秒杀商品的秒杀开始时间戳
                    Long endTime = seckillSkuRedisTo.getEndTime();//秒杀商品的秒杀结束时间戳
                    if (currentTimeMillis >= startTime && currentTimeMillis <= endTime) {
                        //处于秒杀时间段 则直接返回带随机码的商品秒杀信息
                        return seckillSkuRedisTo;
                    }
                    //不处于秒杀时间段 不返回秒杀商品的随机码 将秒杀商品的随机码置空再返回对象给前端
                    seckillSkuRedisTo.setRandomCode(null);
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    /**
     * 秒杀商品
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) {
        long s1 = System.currentTimeMillis();
        //获取当前登录用户的信息
        MemberResponseVo loginUserInfoVo = SecKillInterceptor.threadLocal.get();

        //从redis中获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SECKILL_SKUS_PREFIX);
        String secKillSkuInfo = ops.get(killId);
        if (StringUtils.isEmpty(secKillSkuInfo)) {
            //当前秒杀商品信息为空则直接返回null
            return null;
        }
        //能够获取到当前秒杀商品的信息则进行后续操作
        //将String类型的解析为指定的秒杀商品详细信息对象
        SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(secKillSkuInfo, SeckillSkuRedisTo.class);
        //一系列的合法性校验(当前系统时间见是否处于秒杀时间段 校验秒杀商品的随机码 判断购买数量是否有库存 当前用户是否重复进行秒杀...)
        long currentTimeMillis = System.currentTimeMillis();//当前系统时间戳
        Long startTime = seckillSkuRedisTo.getStartTime();//秒杀商品的开始时间戳
        Long endTime = seckillSkuRedisTo.getEndTime();//秒杀商品的结束时间戳
        if(currentTimeMillis >= startTime && currentTimeMillis <= endTime){//处于秒杀时间段
            String randomCode = seckillSkuRedisTo.getRandomCode();//随机码
            String skuId = seckillSkuRedisTo.getPromotionSessionId() + "-" +seckillSkuRedisTo.getSkuId();//商品id
            if(randomCode.equals(key) && killId.equals(skuId)){//验证随机码和商品id是否都匹配
                Integer seckillLimit = seckillSkuRedisTo.getSeckillLimit();//秒杀数量限制
                String count = redisTemplate.opsForValue().get(SKU_STOCK_SEMAPHORE + randomCode);
                int SecKillCount = Integer.parseInt(count);//信号量数量（秒杀剩余数量）
                if(SecKillCount > 0 && num <= SecKillCount && num <= seckillLimit){//判断秒杀的数量不超过秒杀限制 并且秒杀的数量不超过库存的数量
                    //准备进行秒杀（semaphore）【秒杀幂等性保证 每个用户进行秒杀时存入一个用户秒杀的key作为标志 防止用户重复秒杀】
                    //判断是否同一个用户重复进行秒杀
                    String userSecKillFlag = loginUserInfoVo.getId() + "-" + skuId;//用户进行秒杀时存入一个用户秒杀的key作为标志【用户id-商品id】
                    Long ttl = endTime - currentTimeMillis;//设置用户秒杀标志的过期时间（秒杀结束就自动进行过期）
                    Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(userSecKillFlag, num.toString(), ttl, TimeUnit.MILLISECONDS);
                    if(setIfAbsent){//占位成功表示未秒杀过 可以进行秒杀
                        //semaphore-1操作 表示秒杀
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                        boolean tryAcquire = semaphore.tryAcquire();//快速尝试
                        if(tryAcquire){
                            //TODO 秒杀成功 先快速返回订单号 创建秒杀订单信息给MQ进行削峰处理
                            String id = IdWorker.getTimeId();//使用IdWorker生成订单号
                            //准备订单对象
                            SeckillOrderTo orderTo = new SeckillOrderTo();
                            orderTo.setOrderSn(id);
                            orderTo.setMemberId(loginUserInfoVo.getId());
                            orderTo.setNum(num);
                            orderTo.setPromotionSessionId(seckillSkuRedisTo.getPromotionSessionId());
                            orderTo.setSkuId(seckillSkuRedisTo.getSkuId());
                            orderTo.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());
                            rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);
                            //计算秒杀接口耗时
                            long s2 = System.currentTimeMillis();
                            log.info("秒杀接口耗时..." + (s2 - s1) + " ms");
                            //秒杀完成 返回秒杀单号
                            return id;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 缓存活动相关联的商品信息(Hash数据类型进行储存)
     * @param seckillSessionWithSkusVos
     */
    private void saveSessionSkuInfo(List<SeckillSessionWithSkusVo> seckillSessionWithSkusVos) {
        //遍历所有的SeckillSessionWithSkusVo（活动场次）
        seckillSessionWithSkusVos.stream().forEach(seckillSessionWithSkusVo -> {
            //准备redis的hash数据操作 绑定hash操作
            BoundHashOperations ops = redisTemplate.boundHashOps(SECKILL_SKUS_PREFIX);
            //遍历所有要进行上架的秒杀商品进行上架操作
            seckillSessionWithSkusVo.getRelationSkus().stream()
                    .forEach(relationSku -> {
                        //生成秒杀商品的随机码 防止恶意攻击
                        String token = UUID.randomUUID().toString().replace("_", "");
                        //定义储存到redis中的key promotionId-skuId
                        String key = relationSku.getPromotionSessionId() + "-" + relationSku.getSkuId();
                        if (!ops.hasKey(key)) {//判断redis中是否已经有key了
                            //redis中没有key才进行储存
                            //缓存商品信息
                            // (准备要缓存到redis的TO数据)
                            SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                            //1）远程调用goods服务 根据skuId查询商品的详细信息
                            R info = goodsOpenFeign.info(relationSku.getSkuId());
                            if (info.getCode() == 0) {
                                //调用成功将获取的skuInfoVo数据封装到to对象中
                                SkuInfoVo skuInfoVo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                                });
                                redisTo.setSkuInfo(skuInfoVo);
                            }

                            //2）sku的秒杀信息（属性对拷）
                            BeanUtils.copyProperties(relationSku, redisTo);

                            //3) 设置当前商品的秒杀时间信息
                            redisTo.setStartTime(seckillSessionWithSkusVo.getStartTime().getTime());
                            redisTo.setEndTime(seckillSessionWithSkusVo.getEndTime().getTime());

                            //4) 设置商品的随机码（防止恶意攻击）
                            redisTo.setRandomCode(token);

                            //将to进行json格式序列化 储存到redis 中
                            String stringRedisTo = JSON.toJSONString(redisTo);
                            ops.put(key, stringRedisTo);
                            //如果当前这个场次的商品库存信息已经上架就不需要上架

                            //TODO 使用库存作为分布式锁Redisson信号量[Semaphore]（限流）
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);//锁的name决定锁的粒度
                            //为秒杀商品设置信号量 用商品要进行秒杀的数量
                            semaphore.trySetPermits(relationSku.getSeckillCount());
                        }
                    });
        });
    }

    /**
     * 缓存活动信息(List数据类型进行储存)
     * @param seckillSessionWithSkusVos
     */
    private void saveSessionInfos(List<SeckillSessionWithSkusVo> seckillSessionWithSkusVos) {
        seckillSessionWithSkusVos.stream()
                .forEach(sessionWithSkusVo -> {
                    //获取当前活动开始和结束的时间戳
                    long startTime = sessionWithSkusVo.getStartTime().getTime();
                    long endTime = sessionWithSkusVo.getEndTime().getTime();
                    //定义储存到redis中的key
                    String key = SECKILL_SESSION_PREFIX + startTime + "-" + endTime;//最近三天的活动的key
                    //先判断redis中有没有该信息 没有才进行添加
                    Boolean hasKey = redisTemplate.hasKey(key);
                    if (!hasKey) {
                        //获取活动中所有的商品的skuId
                        List<String> skuIds = sessionWithSkusVo.getRelationSkus().stream()
                                //格式为promotionSessionId（活动场次id）_skuId（商品id）
                                .map(relationSku -> relationSku.getPromotionSessionId() + "-" + relationSku.getSkuId())
                                .collect(Collectors.toList());
                        redisTemplate.opsForList().leftPushAll(key, skuIds);//以List的数据格式储存到redis中
                    }
                });
    }
}
