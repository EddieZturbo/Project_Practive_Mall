package com.eddie.mall_goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;
import com.eddie.mall_goods.dao.CategoryDao;
import com.eddie.mall_goods.entity.CategoryEntity;
import com.eddie.mall_goods.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

}
