package com.eddie.mall_coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eddie.mall_coupon.entity.SeckillSkuRelationEntity;
import com.eddie.mall_coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_coupon.dao.SeckillSessionDao;
import com.eddie.mall_coupon.entity.SeckillSessionEntity;
import com.eddie.mall_coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取最近三天的秒杀活动
     * @return
     */
    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {
        //计算出最近三天
        //查出这三天参与秒杀活动的商品
        List<SeckillSessionEntity> seckillSessionEntityList = this.baseMapper.selectList(
                new LambdaQueryWrapper<SeckillSessionEntity>()
                        .between(//查询开始时间在最近三天范围内的
                                SeckillSessionEntity::getStartTime,
                                startTime(),//当前时间（开始时间）
                                endTime()));//三天后的最后时刻（结束时间）
        if(seckillSessionEntityList != null && seckillSessionEntityList.size() > 0){
            List<SeckillSessionEntity> seckillSessionEntities = seckillSessionEntityList.stream()
                    .map(seckillSessionEntity -> {
                        Long id = seckillSessionEntity.getId();
                        //查出sms_seckill_sku_relation表中关联的skuId
                        List<SeckillSkuRelationEntity> seckillSkuRelationEntityList = seckillSkuRelationService.list(
                                new LambdaQueryWrapper<SeckillSkuRelationEntity>()
                                        .eq(SeckillSkuRelationEntity::getPromotionSessionId, id));
                        seckillSessionEntity.setRelationSkus(seckillSkuRelationEntityList);
                        return seckillSessionEntity;
                    })
                    .collect(Collectors.toList());
            return seckillSessionEntities;
        }
        return null;
    }

    /**
     * 开始时间（第一天'00:00'.）
     * @return
     */
    private String startTime(){
        LocalDate now = LocalDate.now();//当前时间(日期)
        LocalTime min = LocalTime.MIN;//'00:00'.
        LocalDateTime startDateTime = LocalDateTime.of(now, min);//开始的日期时间
        return startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 结束时间(第三天'23:59:59.999999999'.)
     * @return
     */
    private String endTime(){
        LocalDate now = LocalDate.now();//当前时间（日期）
        LocalDate thirdDay = now.plusDays(2);//加两天（第三天）
        LocalTime max = LocalTime.MAX;//'23:59:59.999999999'.
        LocalDateTime thirdDayDateTime = LocalDateTime.of(thirdDay, max);
        return thirdDayDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}