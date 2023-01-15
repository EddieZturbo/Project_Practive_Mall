package com.eddie.mall_goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.eddie.common.constant.ProductConstant;
import com.eddie.mall_goods.dao.AttrAttrgroupRelationDao;
import com.eddie.mall_goods.dao.AttrGroupDao;
import com.eddie.mall_goods.dao.CategoryDao;
import com.eddie.mall_goods.entity.*;
import com.eddie.mall_goods.service.CategoryService;
import com.eddie.mall_goods.vo.AttrRespVo;
import com.eddie.mall_goods.vo.AttrVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_goods.dao.AttrDao;
import com.eddie.mall_goods.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
@Slf4j
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 规格基本信息和关联关系信息
     */
    @Override
    @Transactional
    public void saveAttrVo(AttrVo attrVo) {
        //保存基本信息
        AttrEntity attrEntity = new AttrEntity();
        //将前台页面传输进来的AttrVo对象copy到AttrEntity规格基本信息对象中
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.save(attrEntity);

        //如果是基本参数类型则保存关联信息
        if (attrVo.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && null != attrVo.getAttrGroupId()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils getBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        LambdaQueryWrapper<AttrEntity> queryWrapper = new LambdaQueryWrapper<AttrEntity>().eq(
                AttrEntity::getAttrType
                , "base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if (catelogId != 0) {
            queryWrapper.eq(AttrEntity::getCatelogId, catelogId);
        }
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.eq(AttrEntity::getAttrId, key)
                    .or()
                    .like(AttrEntity::getAttrName, key);
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper);

        List<AttrEntity> records = page.getRecords();

        List<AttrRespVo> attrRespVoResult = records.stream()
                .map((attrEntity) -> {
                    AttrRespVo attrRespVo = new AttrRespVo();
                    BeanUtils.copyProperties(attrEntity, attrRespVo);
                    log.info("attrEntity.getAttrId()\t" + attrEntity.getAttrId().toString());

                    //设置分组的名字
                    if ("base".equalsIgnoreCase(attrType)) {//如果是基本类型才进行分组信息的设置
                        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(
                                new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                                        .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
                        if (null != attrAttrgroupRelationEntity) {
                            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                            log.info(attrGroupEntity.toString());
                            attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());//设置分组的name
                        }
                    }
                    //设置分类的名字
                    CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
                    if (null != categoryEntity) {
                        attrRespVo.setCatelogName(categoryEntity.getName());//设置分类的name
                    }

                    return attrRespVo;
                })
                .collect(Collectors.toList());

        //返回AttrRespVo对象
        //为基本属性添加所属分组名 和所属分类名
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(attrRespVoResult);
        return pageUtils;
    }

    /**
     * 根据分组id查找关联的所有基本属性
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        List<Long> attrIds = entities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        if (attrIds == null || attrIds.size() == 0) {
            return null;
        }
        Collection<AttrEntity> attrEntities = this.listByIds(attrIds);
        return (List<AttrEntity>) attrEntities;
    }

    /**
     * 获取当前分组没有关联的所有属性
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    @Transactional
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1、当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2、当前分组只能关联别的分组没有引用的属性
        //2.1)、当前分类下的其他分组
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = group.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        //2.2)、这些分组关联的属性
        List<AttrAttrgroupRelationEntity> groupId = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        //2.3)、从当前分类的所有属性中移除这些属性；
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;
    }

    @Override
    @Transactional
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();//构造AttrRespVo对象 作为返回结果
        AttrEntity attrEntity = this.getById(attrId);//查询出基本信息

        BeanUtils.copyProperties(attrEntity, attrRespVo);//将查询的出基本信息copy到AttrRespVo对象

        //如果是进本参数类型的话就设置分组信息
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(
                    new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrId,
                            attrId));
            if (null != attrAttrgroupRelationEntity) {
                attrRespVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                if (null != attrGroupEntity) {
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }


        //设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrRespVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (null != categoryEntity) {
            attrRespVo.setCatelogName(categoryEntity.getName());
        }
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //1、修改分组关联
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();

            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());

            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0) {
                attrAttrgroupRelationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }


    }

}