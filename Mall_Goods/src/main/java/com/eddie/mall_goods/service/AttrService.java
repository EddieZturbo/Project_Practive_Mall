package com.eddie.mall_goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.common.utils.PageUtils;
import com.eddie.mall_goods.entity.AttrEntity;
import com.eddie.mall_goods.vo.AttrGroupRelationVo;
import com.eddie.mall_goods.vo.AttrRespVo;
import com.eddie.mall_goods.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:11:48
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 规格基本信息和关联关系信息
     * @param attrVo
     */
    void saveAttrVo(AttrVo attrVo);

    PageUtils getBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    void deleteRelation(List<AttrGroupRelationVo> vos);
}

