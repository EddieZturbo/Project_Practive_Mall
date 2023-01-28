package com.eddie.mall_goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;
import com.eddie.mall_goods.dao.CategoryDao;
import com.eddie.mall_goods.entity.CategoryEntity;
import com.eddie.mall_goods.service.CategoryService;
import com.eddie.mall_goods.vo.Catalog2Vo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listCategories() {
        //获取到分类的所有数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //对分类数据进行层级划分获取
        //1)找到所有的一级分类
        //2）找到一级分类的父子菜单的树结构----找到依次找到每一层的子菜单
        List<CategoryEntity> level1 = categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(0L))//通过filter过滤找到一级分类
                //通过map进行数据处理 setChildrenMenu
                .map((categoryEntity) -> {
                    categoryEntity.setChildMenu(getChildMenus(categoryEntity, categoryEntities));
                    return categoryEntity;
                })
                //通过sorted进行排序 --要进行非空判断使用三元运算
                .sorted(Comparator.comparingInt(value -> value.getSort() == null ? 0 : value.getSort()))
                .collect(Collectors.toList());
        return level1;
    }


    /**
     * 获取childrenMenu
     * @param current
     * @param all
     * @return
     */
    public List<CategoryEntity> getChildMenus(CategoryEntity current, List<CategoryEntity> all) {
        List<CategoryEntity> childrenMenu = all.stream()
                //通过filter过滤找到调用此方法的分类层级的childrenMenu
                //若此categoryEntity的ParentCid（父级id）.equals于调用此方法的分类层级的CatId
                // 则表明就是调用此方法的分类层级的childrenMenu
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(current.getCatId()))
                //递归的方式 继续通过map进行数据处理 setChildrenMenu
                .map((categoryEntity) -> {
                    categoryEntity.setChildMenu(getChildMenus(categoryEntity, all));
                    return categoryEntity;
                })
                //通过sorted进行排序 --要进行非空判断使用三元运算
                .sorted(Comparator.comparingInt((value) -> value.getSort() == null ? 0 : value.getSort()))
                .collect(Collectors.toList());
        return childrenMenu;
    }


    @Override
    public void logicDeleteByIds(List<Long> asList) {
        //TODO 删除前检测是否还有被引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到catelogId的完整路径
     * [父id，儿id，孙id]
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        CategoryEntity category = this.getById(catelogId);
        Long categoryCatId = category.getCatId();//获取当前分类节点的id
        List<Long> path = new ArrayList<>();
        //递归方式寻找当前节点父分类的id以及父分类节点的父分类的id...
        List<Long> fullPath = findCatelogFatherPath(categoryCatId, path);
        Collections.reverse(fullPath);
        return (Long[]) fullPath.toArray(new Long[fullPath.size()]);
    }

    @Override
    public List<CategoryEntity> getLevel1Categories() {
        List<CategoryEntity> categoryEntityList = this.list(
                new LambdaQueryWrapper<CategoryEntity>()
                        .eq(CategoryEntity::getCatLevel, 1));
        return categoryEntityList;
    }

    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        //1、查出所有分类
        //1、1）查出所有一级分类
        List<CategoryEntity> level1Categories = getLevel1Categories();

        //封装数据
        Map<String, List<Catalog2Vo>> parentCid = level1Categories.stream().collect(Collectors.toMap(
                k -> k.getCatId().toString(),
                v -> {
            //1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> level2Catalog = this.list(
                    new LambdaQueryWrapper<CategoryEntity>()
                            .eq(CategoryEntity::getParentCid,v.getCatId()));

            //2、封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (level2Catalog != null) {
                catalog2Vos = level2Catalog.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catalog = this.list(
                            new LambdaQueryWrapper<CategoryEntity>()
                            .eq(CategoryEntity::getParentCid,l2.getCatId()));

                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Catalog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }

            return catalog2Vos;
        }));

        return parentCid;

    }

    public List<Long> findCatelogFatherPath(Long catelogId, List<Long> path) {
        path.add(catelogId);
        Long parentCid = this.getById(catelogId).getParentCid();
        if (0 != parentCid) {
            findCatelogFatherPath(parentCid, path);
        }
        return path;
    }

}
