package com.eddie.mall_goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eddie.common.constant.ProductConstant;
import com.eddie.common.es.SkuEsModel;
import com.eddie.common.to.SkuHasStockTo;
import com.eddie.common.to.SkuReductionTo;
import com.eddie.common.to.SpuBoundTo;
import com.eddie.common.utils.R;
import com.eddie.mall_goods.entity.*;
import com.eddie.mall_goods.feign.CouponOpenFeign;
import com.eddie.mall_goods.feign.SearchOpenFeign;
import com.eddie.mall_goods.feign.WareOpenFeign;
import com.eddie.mall_goods.service.*;
import com.eddie.mall_goods.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    CouponOpenFeign couponOpenFeign;//远程服务调用Mall_Coupon

    @Autowired
    WareOpenFeign wareOpenFeign;//远程服务调用Mall_Ware

    @Autowired
    SearchOpenFeign searchOpenFeign;//远程服务调用Mall_Search


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
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        this.save(spuInfoEntity);

        //保存spu的描述image(操作的表pms_spu_info_desc)
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(StringUtils.join(decript, ","));
        spuInfoDescService.save(spuInfoDescEntity);

        //保存spu的图片集(操作的表pms_spu_images)
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        //保存spu的积分信息(跨库操作project_mall_sms)跨服务(操作的表sms_spu_bounds)
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R rpcResult1 = couponOpenFeign.saveSpuBounds(spuBoundTo);
        if (0 != rpcResult1.getCode()) {
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
        if (null != skus && skus.size() > 0) {
            skus.forEach(item -> {
                List<Images> imagesList = item.getImages();
                String imgUrl = "";//从所有的List<Images>中获取default的img
                for (Images img : imagesList) {
                    if (1 == img.getDefaultImg()) {
                        imgUrl = img.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
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
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (!skuReductionTo.getMemberPrice().isEmpty() || skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1 /*-1, 0, or 1 as this BigDecimal is numerically less than, equal to, or greater than val*/) {
                    //判断是否有存在有效打折或者会员价或者有效满减的数值才进行coupon的提交
                    R rpcResult2 = couponOpenFeign.saveSkuReduction(skuReductionTo);
                    if (0 != rpcResult2.getCode()) {
                        log.error("远程调用Mall_Coupond的@PostMapping(\"mall_coupon/skufullreduction/saveInfo\")失败");
                    }
                }
            });
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SpuInfoEntity> spuInfoEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");//获取检索关键字key
        if (StringUtils.isNotEmpty(key)) {
            spuInfoEntityLambdaQueryWrapper.and(w -> {//相当于and(id = id or name = name)
                w.eq(SpuInfoEntity::getId, key)
                        .or()
                        .like(SpuInfoEntity::getSpuName, key);
            });
        }
        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            spuInfoEntityLambdaQueryWrapper.eq(SpuInfoEntity::getPublishStatus, status);
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            spuInfoEntityLambdaQueryWrapper.eq(SpuInfoEntity::getBrandId, brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(catelogId)) {
            spuInfoEntityLambdaQueryWrapper.eq(SpuInfoEntity::getCatalogId, catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                spuInfoEntityLambdaQueryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * 根据spuId进行商品的上架
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        List<SkuEsModel> skuEsModels = new ArrayList<>();

        //查出当前spuId对应的sku信息以及brand的name
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.list(
                new LambdaQueryWrapper<SkuInfoEntity>()
                        .eq(SkuInfoEntity::getSpuId, spuId));

        //TODO private List<Attrs> attrs;查询出可以进行检索的属性
        //根据spuId查询出所有的ProductAttrValueEntity
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);
        //使用stream流的形式将所有ProductAttrValueEntity中的attr_id收集成list集合
        List<Long> attrIds = productAttrValueEntities.stream()
                .map(productAttrValueEntity -> {
                    return productAttrValueEntity.getAttrId();
                })
                .collect(Collectors.toList());
        //根据attrIds查询pms_attr数据表中search_type（是否可检索字段）为1的所有数据(所有可进行检索的数据的attr_id)
        List<Long> searchAttrIdsList = attrService.searchAttrIds(attrIds);
        //将list集合转成set集合（contains方法效率更好）
        Set<Long> searchAttrIdsSet = new HashSet<>(searchAttrIdsList);
        //SkuEsModel.Attrs对象数据进行封装
        List<SkuEsModel.Attrs> modelAttrs = productAttrValueEntities.stream()
                .filter(productAttrValueEntity -> {
                    //filter进行过滤 只保留需要进行检索的
                    return searchAttrIdsSet.contains(productAttrValueEntity.getAttrId());
                })
                .map(productAttrValueEntity -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(productAttrValueEntity, attrs);
                    return attrs;
                })
                .collect(Collectors.toList());


        //TODO 发送远程服务调用 查看是否有库存
        List<Long> skuIds = skuInfoEntities.stream()
                .map(skuInfoEntity -> {
                    return skuInfoEntity.getSkuId();
                })
                .collect(Collectors.toList());

        R skuHasStock = null;
        try {
            skuHasStock = wareOpenFeign.getSkuHasStock(skuIds);
        } catch (Exception e) {
            log.info("远程调用库存服务异常; 异常原因:{}", e);
        }


        //组装需要的数据
        R finalSkuHasStock = skuHasStock;
        List<SkuEsModel> skuEsModelList = skuInfoEntities.stream()
                .map(skuInfoEntity -> {
                    SkuEsModel skuEsModel = new SkuEsModel();
                    //共同的属性进行对拷
                    BeanUtils.copyProperties(skuInfoEntity, skuEsModel);
                    //个别不一致属性name的单独进行赋值处理
                    //private BigDecimal skuPrice;  private String skuImg;
                    skuEsModel.setSkuPrice(skuInfoEntity.getPrice());
                    skuEsModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());
                    //private Boolean hasStock;
                    Object data = finalSkuHasStock.get("data");
                    //TODO 为解决类型转换解析错误需要使用JSON进行序列化再指定类型的反序列化
                    String toJSONString = JSON.toJSONString(data);
                    TypeReference<List<SkuHasStockTo>> typeReference = new TypeReference<List<SkuHasStockTo>>() {
                    };
                    List<SkuHasStockTo> skuHasStockToList = JSON.parseObject(toJSONString, typeReference);


                    if (skuHasStockToList != null || skuHasStockToList.size() != 0) {
                        Optional<SkuHasStockTo> hasStockTo = skuHasStockToList.stream()
                                .filter(skuHasStockTo -> {
                                    return skuHasStockTo.getSkuId() == skuEsModel.getSkuId();
                                })
                                .findAny();
                        skuEsModel.setHasStock(hasStockTo.get().getHasStock());
                    } else {
                        skuEsModel.setHasStock(false);
                    }
                    //private Long hotScore;TODO 热度评分 目前后端系统未进行开发处理 可以设置默认值为0
                    skuEsModel.setHotScore(0L);
                    //TODO 品牌和分类的信息
                    //private Long brandId;private String brandName;private String brandImg;
                    BrandEntity brandEntity = brandService.getById(skuInfoEntity.getBrandId());
                    skuEsModel.setBrandId(brandEntity.getBrandId());
                    skuEsModel.setBrandName(brandEntity.getName());
                    skuEsModel.setBrandImg(brandEntity.getLogo());
                    //private Long catalogId;private String catalogName;
                    CategoryEntity categoryEntity = categoryService.getById(skuInfoEntity.getCatalogId());
                    skuEsModel.setCatalogId(categoryEntity.getCatId());
                    skuEsModel.setCatalogName(categoryEntity.getName());

                    //赋值检索属性SkuEsModel.Attrs
                    skuEsModel.setAttrs(modelAttrs);

                    return skuEsModel;
                })
                .collect(Collectors.toList());

        //TODO 将数据发送给ElasticSearch进行保存
        R saveGoods = searchOpenFeign.saveGoods(skuEsModelList);
        if(saveGoods.getCode() == 0){
            //远程调用成功
            //修改pms_spu_info表中的publish_status字段(上架状态[0 - 下架，1 - 上架])
            SpuInfoEntity spuInfoEntity = this.getById(spuId);
            spuInfoEntity.setPublishStatus(ProductConstant.StatusEnum.UP_SPU.getCode());
            this.updateById(spuInfoEntity);
        }else{
            //远程调用失败
            //TODO 重复调用问题（接口幂等性）重试机制

        }
    }


}