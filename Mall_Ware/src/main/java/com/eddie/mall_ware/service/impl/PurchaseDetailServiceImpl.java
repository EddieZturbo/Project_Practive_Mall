package com.eddie.mall_ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_ware.dao.PurchaseDetailDao;
import com.eddie.mall_ware.entity.PurchaseDetailEntity;
import com.eddie.mall_ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseDetailEntity> purchaseDetailEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            purchaseDetailEntityLambdaQueryWrapper.and(w -> {
                w.eq(PurchaseDetailEntity::getPurchaseId, key)
                        .or()
                        .eq(PurchaseDetailEntity::getSkuId, key);
            });

        }
        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            purchaseDetailEntityLambdaQueryWrapper.eq(PurchaseDetailEntity::getStatus,status);
        }
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            purchaseDetailEntityLambdaQueryWrapper.eq(PurchaseDetailEntity::getWareId,wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                purchaseDetailEntityLambdaQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        LambdaQueryWrapper<PurchaseDetailEntity> purchaseDetailEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<PurchaseDetailEntity> purchaseDetailEntities = this.list(purchaseDetailEntityLambdaQueryWrapper.eq(PurchaseDetailEntity::getPurchaseId, id));
        return purchaseDetailEntities;
    }

}