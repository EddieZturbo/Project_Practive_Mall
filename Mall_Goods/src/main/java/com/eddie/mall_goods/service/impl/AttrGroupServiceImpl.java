package com.eddie.mall_goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
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

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<AttrGroupEntity> queryWrapper = new LambdaQueryWrapper<>();
        //select * from pms_attr_group where catelogId = ? and (attr_group_id = ? or attr_group_name = ?)
        if(!StringUtils.isEmpty(key)){//如果有key（额外的查询条件attr_group_id = ? or attr_group_name = ?）
            queryWrapper.and((obj) -> {
                obj.eq(AttrGroupEntity::getAttrGroupId,key)
                        .or()
                        .like(AttrGroupEntity::getAttrGroupName,key);
            });
        }
        if(catelogId == 0){
            //如果没有传递catelogId属性值 就默认使用AttrGroupServiceImpl.page()方法进行查询所有的分类
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),queryWrapper);
            return new PageUtils(page);
        }else{
            queryWrapper.eq(AttrGroupEntity::getCatelogId,catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),queryWrapper);
            return new PageUtils(page);
        }
    }

}