package com.eddie.mall_goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.common.utils.PageUtils;
import com.eddie.mall_goods.entity.ProductAttrValueEntity;

import java.util.Map;

/**
 * spu属性值
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:11:48
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

