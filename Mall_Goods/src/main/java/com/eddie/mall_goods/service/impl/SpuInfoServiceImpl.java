package com.eddie.mall_goods.service.impl;

import com.eddie.common.to.SkuReductionTo;
import com.eddie.common.to.SpuBoundTo;
import com.eddie.common.utils.R;
import com.eddie.mall_goods.entity.*;
import com.eddie.mall_goods.feign.CouponOpenFeign;
import com.eddie.mall_goods.service.*;
import com.eddie.mall_goods.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_goods.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;

//TODO 高级部分继续优化完善
@Service("spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponOpenFeign couponOpenFeign;//远程服务调用Mall_Coupon

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        //①保存spu基本信息(操作的表pms_spu_info    pms_spu_images  pms_spu_info_desc)
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo,spuInfoEntity);
        this.save(spuInfoEntity);

            //保存spu的描述image(操作的表pms_spu_info_desc)
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(StringUtils.join(decript,","));
        spuInfoDescService.save(spuInfoDescEntity);

                //保存spu的图片集(操作的表pms_spu_images)
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);

                    //保存spu的积分信息(跨库操作project_mall_sms)跨服务(操作的表sms_spu_bounds)
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R rpcResult1 = couponOpenFeign.saveSpuBounds(spuBoundTo);
        if (0 != rpcResult1.getCode()){
            log.error("远程调用Mall_Coupond的@PostMapping(\"mall_coupon/coupon/save\")失败");
        }

        //②保存spu的规格参数BaseAttrs(操作的表pms_product_attr_value)
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream()
                .map(baseAttr -> {
                    ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                    productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                    productAttrValueEntity.setAttrName(attrService.getById(baseAttr.getAttrId()).getAttrName());
                    productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
                    productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
                    productAttrValueEntity.setSpuId(spuInfoEntity.getId());
                    return productAttrValueEntity;
                })
                .collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);

        //③保存当前spu对应的sku信息
        //3.1)保存sku的基本信息(操作的表pms_sku_info)
                //3.2)保存sku的image(操作的表pms_sku_images)
                    //3.3)保存sku的销售属性(操作的表pms_sku_sale_attr_value)
        List<Skus> skus = spuSaveVo.getSkus();
        if (null != skus && skus.size() > 0){
            skus.forEach(item -> {
                List<Images> imagesList = item.getImages();
                String imgUrl = "";//从所有的List<Images>中获取default的img
                for (Images img : imagesList) {
                    if (1 == img.getDefaultImg()) {
                        imgUrl = img.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSkuDefaultImg(imgUrl);//default的img
                skuInfoService.save(skuInfoEntity);//3.1)保存sku的基本信息(操作的表pms_sku_info)

                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> skuImagesEntities = imagesList.stream()
                        .filter(skuImagesEntity -> {
                            //返回false就会被过滤掉 符合过滤条件的才能继续留在流中
                            return StringUtils.isNotEmpty(skuImagesEntity.getImgUrl());
                        })
                        .map(image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setImgUrl(image.getImgUrl());
                            skuImagesEntity.setDefaultImg(image.getDefaultImg());
                            return skuImagesEntity;
                        })
                        .collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);//3.2)保存sku的image(操作的表pms_sku_images)


                List<Attr> skuSaleAttrValues = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleAttrValues.stream()
                        .map(skuSaleAttrValue -> {
                            SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                            BeanUtils.copyProperties(skuSaleAttrValue, skuSaleAttrValueEntity);
                            skuSaleAttrValueEntity.setSkuId(skuId);
                            return skuSaleAttrValueEntity;
                        })
                        .collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);//3.3)保存sku的销售属性(操作的表pms_sku_sale_attr_value)
         //④保存sku的优惠满减会员价格积分等信息(跨库操作project_mall_sms)跨服务
            //优惠信息(操作的表sms_sku_ladder)
                //满减信息(操作的表sms_sku_full_reduction)
                    //积分等信息(操作的表sms_spu_bounds)
                        //会员价格信息(操作的表sms_member_price)
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(!skuReductionTo.getMemberPrice().isEmpty() || skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1 /*-1, 0, or 1 as this BigDecimal is numerically less than, equal to, or greater than val*/){
                    //判断是否有存在有效打折或者会员价或者有效满减的数值才进行coupon的提交
                    R rpcResult2 = couponOpenFeign.saveSkuReduction(skuReductionTo);
                    if (0 != rpcResult2.getCode()){
                        log.error("远程调用Mall_Coupond的@PostMapping(\"mall_coupon/skufullreduction/saveInfo\")失败");
                    }
                }
            });
        }
    }

}