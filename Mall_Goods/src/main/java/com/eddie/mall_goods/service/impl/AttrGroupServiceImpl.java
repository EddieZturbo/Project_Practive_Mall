package com.eddie.mall_goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eddie.mall_goods.entity.AttrEntity;
import com.eddie.mall_goods.service.AttrService;
import com.eddie.mall_goods.vo.AttrGroupWithAttrsVo;
import com.eddie.mall_goods.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
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

import com.eddie.mall_goods.dao.AttrGroupDao;
import com.eddie.mall_goods.entity.AttrGroupEntity;
import com.eddie.mall_goods.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catalogId) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<AttrGroupEntity> queryWrapper = new LambdaQueryWrapper<>();
        //select * from pms_attr_group where catalogId = ? and (attr_group_id = ? or attr_group_name = ?)
        if(!StringUtils.isEmpty(key)){//如果有key（额外的查询条件attr_group_id = ? or attr_group_name = ?）
            queryWrapper.and((obj) -> {
                obj.eq(AttrGroupEntity::getAttrGroupId,key)
                        .or()
                        .like(AttrGroupEntity::getAttrGroupName,key);
            });
        }
        if(catalogId == 0){
            //如果没有传递catalogId属性值 就默认使用AttrGroupServiceImpl.page()方法进行查询所有的分类
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),queryWrapper);
            return new PageUtils(page);
        }else{
            queryWrapper.eq(AttrGroupEntity::getCatalogId,catalogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),queryWrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatalogId(Long catalogId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new LambdaQueryWrapper<AttrGroupEntity>().eq(AttrGroupEntity::getCatalogId, catalogId));
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupEntities.stream()
                .map(item -> {
                    AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
                    BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
                    List<AttrEntity> relationAttrs = attrService.getRelationAttr(item.getAttrGroupId());
                    attrGroupWithAttrsVo.setAttrs(relationAttrs);
                    return attrGroupWithAttrsVo;
                })
                .collect(Collectors.toList());
        return attrGroupWithAttrsVos;
    }

    /**
     * 根据spuId查询出对应的所有属性的分组信息以及当前分组下的所有属性对应的值
     * @param spuId
     * @param catalogId
     * @return
     */
    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySkuId(Long spuId, Long catalogId) {
        return this.baseMapper.getAttrGroupWithAttrsBySkuId(spuId,catalogId);
    }

}