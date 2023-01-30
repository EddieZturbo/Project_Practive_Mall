package com.eddie.mall_goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private RedisTemplate redisTemplate;

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
     * @param catalogId
     * @return
     */
    @Override
    public Long[] findCatalogPath(Long catalogId) {
        CategoryEntity category = this.getById(catalogId);
        Long categoryCatId = category.getCatId();//获取当前分类节点的id
        List<Long> path = new ArrayList<>();
        //递归方式寻找当前节点父分类的id以及父分类节点的父分类的id...
        List<Long> fullPath = findCatalogFatherPath(categoryCatId, path);
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
        //1)先从缓存中获取
        Object jsonFromRedis = redisTemplate.opsForValue().get("catalogJson");
        if (null == jsonFromRedis) {//缓存中没有数据
            //2）若缓存中没有则从数据库中获取数据 获取到数据后将数据缓存到redis中
            Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDB();//从数据库中获取数据
            if (null == catalogJsonFromDB) {//数据库中也查询不到
                //TODO 处理缓存穿透问题
                //缓存一个空对象 60s有效
                log.info("缓存击穿");
                redisTemplate.opsForValue().set("catalogJson", "null", 60, TimeUnit.MINUTES);
            }
            log.info("从数据库中获取到数据");
            redisTemplate.opsForValue().set("catalogJson", catalogJsonFromDB, 30, TimeUnit.MINUTES);//缓存到redis中 30分钟失效
            log.info("将数据缓存到redis中");
            return catalogJsonFromDB;//返回从数据库中查询到的数据
        }

        //如果缓存中查询到的是"null" 则表明缓存击穿
        String toJSONString = JSON.toJSONString(jsonFromRedis);
        if("null".equals(toJSONString)){
            Map<String, List<Catalog2Vo>> nullValue = new HashMap<>();
            nullValue.put("null",null);
            return nullValue;//返回空对象
        }

        //缓存中查询到数据直接返回
        log.info("从缓存中获取到数据");
        Map<String, List<Catalog2Vo>> catalogJson = JSON.parseObject(
                toJSONString,
                new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
        return catalogJson;
    }

    /**
     * 从数据库中获取数据
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDB() {
        //1、TODO 查出所有分类(只需要查询一次数据库 将三级分类的数据全部查询出来 抽取出一个公共的方法 进行二级分类&三级分类菜单的查询)
        List<CategoryEntity> categoryEntityList = this.list(null);//查出三级分类菜单

        //1、1）查出所有一级分类
        List<CategoryEntity> level1Categories = getLevel1Categories();


        //封装数据
        Map<String, List<Catalog2Vo>> parentCid = level1Categories.stream().collect(Collectors.toMap(
                k -> k.getCatId().toString(),
                v -> {
                    //1、每一个的一级分类,查到这个一级分类的二级分类
                    List<CategoryEntity> level2Catalog = getChildLevelCatalog(categoryEntityList, v);

                    //2、封装上面的结果
                    List<Catalog2Vo> catalog2Vos = null;
                    if (level2Catalog != null) {
                        catalog2Vos = level2Catalog.stream().map(l2 -> {
                            Catalog2Vo catalog2Vo = new Catalog2Vo(
                                    v.getCatId().toString()
                                    , null
                                    , l2.getCatId().toString()
                                    , l2.getName().toString());

                            //1、找当前二级分类的三级分类封装成vo
                            List<CategoryEntity> level3Catalog = getChildLevelCatalog(categoryEntityList, l2);

                            if (level3Catalog != null) {
                                List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Catalog.stream().map(l3 -> {
                                    //2、封装成指定格式
                                    Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(
                                            l2.getCatId().toString()
                                            , l3.getCatId().toString()
                                            , l3.getName());

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

    /**
     * TODO 抽取的方法 根据查询出来的三级分类数据 通过stream的形式过滤出 二级分类&三级分类
     * @param categoryEntities
     * @param currentCategory
     * @return
     */
    private List<CategoryEntity> getChildLevelCatalog(List<CategoryEntity> categoryEntities, CategoryEntity currentCategory) {
        List<CategoryEntity> categoryEntityList = categoryEntities.stream()
                .filter(item -> item.getParentCid().equals(currentCategory.getCatId()))
                .collect(Collectors.toList());
        return categoryEntityList;
    }

    public List<Long> findCatalogFatherPath(Long catalogId, List<Long> path) {
        path.add(catalogId);
        Long parentCid = this.getById(catalogId).getParentCid();
        if (0 != parentCid) {
            findCatalogFatherPath(parentCid, path);
        }
        return path;
    }

}
