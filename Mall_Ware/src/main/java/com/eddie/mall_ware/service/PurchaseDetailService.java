package com.eddie.mall_ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.common.utils.PageUtils;
import com.eddie.mall_ware.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-16 21:20:00
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

