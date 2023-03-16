package com.eddie.mall_coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_coupon.dao.SeckillSkuRelationDao;
import com.eddie.mall_coupon.entity.SeckillSkuRelationEntity;
import com.eddie.mall_coupon.service.SeckillSkuRelationService;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<SeckillSkuRelationEntity> queryWrapper = new LambdaQueryWrapper<SeckillSkuRelationEntity>();
        String promotionSessionId = (String) params.get("promotionSessionId");
        //如果前端的查询携带了promotionSessionId这表明查询指定promotionSessionId的数据
        if(StringUtils.isNotEmpty(promotionSessionId)){
            queryWrapper.eq(SeckillSkuRelationEntity::getPromotionSessionId,promotionSessionId);
        }
        //若前端的查询请求未携带promotionSessionId则表明查询所有的数据z
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),queryWrapper
        );

        return new PageUtils(page);
    }

}