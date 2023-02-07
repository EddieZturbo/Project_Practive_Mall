package com.eddie.mall_goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.common.utils.PageUtils;
import com.eddie.mall_goods.entity.AttrGroupEntity;
import com.eddie.mall_goods.vo.AttrGroupWithAttrsVo;
import com.eddie.mall_goods.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:11:48
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catalogId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatalogId(Long catalogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySkuId(Long spuId, Long catalogId);
}

