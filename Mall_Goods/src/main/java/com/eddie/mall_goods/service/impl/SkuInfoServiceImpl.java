package com.eddie.mall_goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eddie.mall_goods.entity.SkuImagesEntity;
import com.eddie.mall_goods.entity.SpuInfoDescEntity;
import com.eddie.mall_goods.service.*;
import com.eddie.mall_goods.vo.SeckillSkuVo;
import com.eddie.mall_goods.vo.SkuItemSaleAttrVo;
import com.eddie.mall_goods.vo.SkuItemVo;
import com.eddie.mall_goods.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_goods.dao.SkuInfoDao;
import com.eddie.mall_goods.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SkuInfoEntity> skuInfoEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            skuInfoEntityLambdaQueryWrapper.and(w -> {
                w.eq(SkuInfoEntity::getSkuId, key)
                        .or()
                        .like(SkuInfoEntity::getSkuName, key);
            });
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            skuInfoEntityLambdaQueryWrapper.eq(SkuInfoEntity::getBrandId, brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            skuInfoEntityLambdaQueryWrapper.eq(SkuInfoEntity::getCatalogId, catelogId);
        }
        String min = (String) params.get("min");
        if (StringUtils.isNotEmpty(min)) {
            skuInfoEntityLambdaQueryWrapper.ge(SkuInfoEntity::getPrice, min);
        }
        String max = (String) params.get("max");
        if (StringUtils.isNotEmpty(max)) {
            try {
                //Throws:
                //NumberFormatException – if val is not a valid representation of a BigDecimal
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal(0)) == 1) {
                    skuInfoEntityLambdaQueryWrapper.le(SkuInfoEntity::getPrice, max);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                skuInfoEntityLambdaQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        //1、sku基本信息的获取  pms_sku_info(异步执行★)
        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            //异步任务获取info
            SkuInfoEntity info = this.getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleFuture = skuInfoFuture.thenAcceptAsync((info) -> {
            //获取info的异步任务完成后 拿到异步任务的结果info 并进行以下操作
            //3、获取spu的销售属性组合(等★完成后异步执行)
            List<SkuItemSaleAttrVo> skuItemSaleAttrVos = skuSaleAttrValueService.getSaleAttrValueBySpuId(info.getSpuId());
            skuItemVo.setSaleAttr(skuItemSaleAttrVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> descFuture = skuInfoFuture.thenAcceptAsync((info) -> {
            //4、获取spu的介绍    pms_spu_info_desc(等★完成后异步执行)
            //根据查询到的sku的info获取到spu的id从而查询到对应的SpuInfoDescEntity
            SpuInfoDescEntity desc = spuInfoDescService.getById(info.getSpuId());
            skuItemVo.setDesc(desc);
        }, threadPoolExecutor);

        CompletableFuture<Void> groupAttrFuture = skuInfoFuture.thenAcceptAsync((info) -> {
            //5、获取spu的规格参数信息(等★完成后异步执行)
            List<SpuItemAttrGroupVo> groupWithAttrsBySkuId = attrGroupService.getAttrGroupWithAttrsBySkuId(info.getSpuId(), info.getCatalogId());
            skuItemVo.setGroupAttrs(groupWithAttrsBySkuId);
        }, threadPoolExecutor);


        //2、sku的图片信息    pms_sku_images(异步执行☆)
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);


        //等到所有任务都完成(get方法会阻塞主线程)
        CompletableFuture.allOf(/*skuInfoFuture,可以省略；因为后续的三个只有它完成才会执行 */saleFuture,descFuture,groupAttrFuture,imagesFuture).get();
        return skuItemVo;
    }

}