package com.eddie.mall_goods.dao;

import com.eddie.mall_goods.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:11:48
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    /**
     * 批量删除关联关系
     * @param attrAttrgroupRelationEntities
     */
    void deleteBatchRelation(@Param("attrAttrgroupRelationEntities") List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities);
}
