package com.eddie.mall_ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_ware.dao.WareSkuDao;
import com.eddie.mall_ware.entity.WareSkuEntity;
import com.eddie.mall_ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> wareSkuEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(StringUtils.isNotEmpty(skuId)){
            wareSkuEntityLambdaQueryWrapper.eq(WareSkuEntity::getSkuId,skuId);
        }
        String wareId = (String) params.get("wareId");
        if(StringUtils.isNotEmpty(skuId)){
            wareSkuEntityLambdaQueryWrapper.eq(WareSkuEntity::getWareId,wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wareSkuEntityLambdaQueryWrapper
        );

        return new PageUtils(page);
    }

}