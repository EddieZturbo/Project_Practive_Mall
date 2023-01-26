package com.eddie.mall_ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.to.SkuHasStockTo;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;
import com.eddie.common.utils.R;
import com.eddie.mall_ware.dao.WareSkuDao;
import com.eddie.mall_ware.entity.WareSkuEntity;
import com.eddie.mall_ware.feign.GoodsOpenFeign;
import com.eddie.mall_ware.service.WareSkuService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    WareSkuService wareSkuService;

    @Autowired
    GoodsOpenFeign goodsOpenFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> wareSkuEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)) {
            wareSkuEntityLambdaQueryWrapper.eq(WareSkuEntity::getSkuId, skuId);
        }
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(skuId)) {
            wareSkuEntityLambdaQueryWrapper.eq(WareSkuEntity::getWareId, wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wareSkuEntityLambdaQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> list = wareSkuService.list(new LambdaQueryWrapper<WareSkuEntity>().eq(WareSkuEntity::getSkuId, skuId));
        //1、判断如果还没有这个库存记录新增
        if (null == list || list.size() == 0) {
            //新增库存记录
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = goodsOpenFeign.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                if (0 == info.getCode()) {
                    skuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuService.save(skuEntity);//进行新增库存记录操作
        } else {
            //add库存
            wareSkuService.appendStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public void appendStock(Long skuId, Long wareId, Integer skuNum) {
        wareSkuDao.appendStock(skuId, wareId, skuNum);
    }

    @Override
    public List<SkuHasStockTo> skuHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> skuHasStockVos = skuIds.stream()
                .map(skuId -> {
                    SkuHasStockTo skuHasStockVo = new SkuHasStockTo();
                    //查询当前sku的总库存量
                    Long count = baseMapper.getSkuStock(skuId);
                    skuHasStockVo.setSkuId(skuId);
                    skuHasStockVo.setHasStock(count == null?false : count > 0);//设置是否有库存
                    return skuHasStockVo;
                })
                .collect(Collectors.toList());
        return skuHasStockVos;
    }

}