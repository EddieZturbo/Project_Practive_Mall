package com.eddie.mall_goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.eddie.mall_goods.entity.BrandEntity;
import com.eddie.mall_goods.entity.CategoryEntity;
import com.eddie.mall_goods.service.BrandService;
import com.eddie.mall_goods.service.CategoryService;
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
import org.springframework.transaction.annotation.Transactional;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Autowired
    CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    CategoryService categoryService;

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
                new LambdaQueryWrapper<CategoryBrandRelationEntity>().eq(CategoryBrandRelationEntity::getCatalogId, catId));

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

    /**
     * 根据前端传来的{"brandId":7,"catelogId":225} 查询出相应的name 一同save至pms_category_brand_relation表中
     * @param categoryBrandRelation
     */
    @Override
    @Transactional
    public void saveWithName(CategoryBrandRelationEntity categoryBrandRelation) {
        //分别从pms_brand和pms_category数据表中根据id查询相应的对象
        BrandEntity brandEntity = brandService.getById(categoryBrandRelation.getBrandId());
        CategoryEntity categoryEntity = categoryService.getById(categoryBrandRelation.getCatalogId());

        //将查寻到的pms_brand和pms_category的name 赋值给到CategoryBrandRelationEntity
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatalogName(categoryEntity.getName());

        //并操作pms_category_brand_relation数据表进行保存
        categoryBrandRelationDao.insert(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setBrandId(brandId);
        relationEntity.setBrandName(name);
        this.update(relationEntity,new LambdaQueryWrapper<CategoryBrandRelationEntity>().eq(CategoryBrandRelationEntity::getBrandId,brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        baseMapper.updateCategory(catId,name);
    }


}