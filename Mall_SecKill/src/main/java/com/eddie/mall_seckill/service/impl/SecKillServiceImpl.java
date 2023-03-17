package com.eddie.mall_seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eddie.common.utils.R;
import com.eddie.mall_seckill.feign.CouponOpenFeign;
import com.eddie.mall_seckill.feign.GoodsOpenFeign;
import com.eddie.mall_seckill.service.SecKillService;
import com.eddie.mall_seckill.to.SeckillSkuRedisTo;
import com.eddie.mall_seckill.vo.SeckillSessionWithSkusVo;
import com.eddie.mall_seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 @author EddieZhang
 @create 2023-03-17 9:19 AM
 */
@Service
public class SecKillServiceImpl implements SecKillService {
    private static final String SECKILL_SESSION_PREFIX = "seckill:sessions:";//redis的key--秒杀活动的key
    private static final String SECKILL_SKUS_PREFIX = "seckill:skus:";//redis的key--秒杀活动参与秒杀的所有商品的key
    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:semaphore:";    //+商品随机码
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    CouponOpenFeign couponOpenFeign;
    @Autowired
    GoodsOpenFeign goodsOpenFeign;
    @Autowired
    RedissonClient redissonClient;

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
                        String key = relationSku.getPromotionId() + "-" + relationSku.getSkuId();
                        if (!ops.hasKey(key)) {//判断redis中是否已经有key了
                            //redis中没有key才进行储存
                            //缓存商品信息
                            // (准备要缓存到redis的TO数据)
                            SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                            //1）远程调用goods服务 根据skuId查询商品的详细信息
                            R info = goodsOpenFeign.info(relationSku.getSkuId());
                            if (info.getCode() == 0) {
                                //调用成功将获取的skuInfoVo数据封装到to对象中
                                SkuInfoVo skuInfoVo = info.getData("data", new TypeReference<SkuInfoVo>() {
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
                                .map(relationSku -> relationSku.getPromotionSessionId() + "_" + relationSku.getSkuId())
                                .collect(Collectors.toList());
                        redisTemplate.opsForList().leftPush(key, skuIds);//以List的数据格式储存到redis中
                    }
                });
    }
}
