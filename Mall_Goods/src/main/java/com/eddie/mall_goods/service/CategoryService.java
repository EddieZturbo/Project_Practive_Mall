package com.eddie.mall_goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.common.utils.PageUtils;
import com.eddie.mall_goods.entity.CategoryEntity;
import com.eddie.mall_goods.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:11:48
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listCategories();

    void logicDeleteByIds(List<Long> asList);
    /**
     * 找到catelogId的完整路径
     * [父id，儿id，孙id]
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    List<CategoryEntity> getLevel1Categories();

    Map<String,List<Catalog2Vo>> getCatalogJson();
}

