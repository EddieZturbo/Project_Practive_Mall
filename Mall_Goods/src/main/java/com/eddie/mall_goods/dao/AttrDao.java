package com.eddie.mall_goods.dao;

import com.eddie.mall_goods.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:11:48
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    /**
     * 根据attrIds查询所有可进行检索(search_type为1)的数据的attr_id
     * @param attrIds
     * @return
     */
    List<Long> searchAttrIds(@Param("attrIds") List<Long> attrIds);
}
