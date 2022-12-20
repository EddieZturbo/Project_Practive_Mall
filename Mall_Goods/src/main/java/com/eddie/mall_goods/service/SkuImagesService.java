package com.eddie.mall_goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.common.utils.PageUtils;
import com.eddie.mall_goods.entity.SkuImagesEntity;

import java.util.Map;

/**
 * sku图片
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:11:48
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

