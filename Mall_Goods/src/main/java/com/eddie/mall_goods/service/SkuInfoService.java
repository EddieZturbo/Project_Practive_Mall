package com.eddie.mall_goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.common.utils.PageUtils;
import com.eddie.mall_goods.entity.SkuInfoEntity;
import com.eddie.mall_goods.vo.SkuItemVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:11:48
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;
}

