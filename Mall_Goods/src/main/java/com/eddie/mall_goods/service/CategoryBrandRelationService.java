package com.eddie.mall_goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.common.utils.PageUtils;
import com.eddie.mall_goods.entity.BrandEntity;
import com.eddie.mall_goods.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:11:47
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<BrandEntity> getBrandsByCatId(Long catId);

    /**
     * 根据前端传来的{"brandId":7,"catelogId":225} 查询出相应的name 一同save至pms_category_brand_relation表中
     * @param categoryBrandRelation
     */
    void saveWithName(CategoryBrandRelationEntity categoryBrandRelation);


    void updateBrand(Long brandId, String name);
}

