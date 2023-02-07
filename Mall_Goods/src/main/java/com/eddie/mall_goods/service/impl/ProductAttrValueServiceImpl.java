package com.eddie.mall_goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_goods.dao.ProductAttrValueDao;
import com.eddie.mall_goods.entity.ProductAttrValueEntity;
import com.eddie.mall_goods.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据spuId查询出所有的ProductAttrValueEntity
     * @param spuId
     * @return
     */
    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        List<ProductAttrValueEntity> entities = this.list(
                new LambdaQueryWrapper<ProductAttrValueEntity>()
                        .eq(ProductAttrValueEntity::getSpuId, spuId));
        return entities;
    }

    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities) {
        //1、删除这个spuId之前对应的所有属性
        this.remove(
                new LambdaQueryWrapper<ProductAttrValueEntity>()
                        .eq(ProductAttrValueEntity::getSpuId, spuId));

        List<ProductAttrValueEntity> collect = entities.stream()
                .map(item -> {
                    item.setSpuId(spuId);
                    return item;
                }).collect(Collectors.toList());
        this.saveBatch(collect);
    }

}