package com.eddie.mall_ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.common.to.OrderTo;
import com.eddie.common.to.SkuHasStockTo;
import com.eddie.common.to.mq.StockLockedTo;
import com.eddie.common.utils.PageUtils;
import com.eddie.mall_ware.entity.WareSkuEntity;
import com.eddie.mall_ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-16 21:20:00
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    void appendStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockTo> skuHasStock(List<Long> skuIds);

    boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo to);

    void unlockStock(OrderTo to);
}

