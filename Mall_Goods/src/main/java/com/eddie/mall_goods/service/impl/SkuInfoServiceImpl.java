package com.eddie.mall_goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_goods.dao.SkuInfoDao;
import com.eddie.mall_goods.entity.SkuInfoEntity;
import com.eddie.mall_goods.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SkuInfoEntity> skuInfoEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if(StringUtils.isNotEmpty(key)){
            skuInfoEntityLambdaQueryWrapper.and(w -> {
                w.eq(SkuInfoEntity::getSkuId,key)
                        .or()
                        .like(SkuInfoEntity::getSkuName,key);
            });
        }
        String brandId = (String) params.get("brandId");
        if(StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            skuInfoEntityLambdaQueryWrapper.eq(SkuInfoEntity::getBrandId,brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if(StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            skuInfoEntityLambdaQueryWrapper.eq(SkuInfoEntity::getCatalogId,catelogId);
        }
        String min = (String) params.get("min");
        if(StringUtils.isNotEmpty(min)){
            skuInfoEntityLambdaQueryWrapper.ge(SkuInfoEntity::getPrice,min);
        }
        String max = (String) params.get("max");
        if(StringUtils.isNotEmpty(max)){
            try {
                //Throws:
                //NumberFormatException – if val is not a valid representation of a BigDecimal
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal(0)) == 1){
                    skuInfoEntityLambdaQueryWrapper.le(SkuInfoEntity::getPrice,max);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                skuInfoEntityLambdaQueryWrapper
        );

        return new PageUtils(page);
    }

}