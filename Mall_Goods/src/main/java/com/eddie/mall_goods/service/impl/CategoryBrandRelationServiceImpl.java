package com.eddie.mall_goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eddie.mall_goods.entity.BrandEntity;
import com.eddie.mall_goods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_goods.dao.CategoryBrandRelationDao;
import com.eddie.mall_goods.entity.CategoryBrandRelationEntity;
import com.eddie.mall_goods.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Autowired
    CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据分类id 查询品牌数据
     * @param catId
     * @return
     */
    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        //根据传来的分类id(catID)通过查询pms_attr_attrgroup_relation表中的数据
        List<CategoryBrandRelationEntity> categoryBrandRelationEntities = categoryBrandRelationDao.selectList(
                new LambdaQueryWrapper<CategoryBrandRelationEntity>().eq(CategoryBrandRelationEntity::getCatelogId, catId));

        //通过stream的形式将查出的分类下的品牌的id作为查询条件 并通过brandService查询出相应的完整品牌数据
        List<BrandEntity> brandEntityList = categoryBrandRelationEntities.stream()
                .map(item -> {
                    Long brandId = item.getBrandId();
                    BrandEntity brandEntity = brandService.getById(brandId);
                    return brandEntity;
                })
                .collect(Collectors.toList());
        return brandEntityList;
    }


}