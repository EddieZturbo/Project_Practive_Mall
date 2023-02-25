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
import com.eddie.mall_goods.service.CategoryBrandRelationService;
import com.eddie.mall_goods.service.CategoryService;
import com.eddie.mall_goods.vo.Catalog2Vo;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

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

    @Cacheable(cacheNames = {"Categories"}//指定缓存的类名
            ,key = "#root.methodName")//指定这一类缓存下的key名
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("getLevel1Categories()方法调用");
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
            Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();//从数据库中获取数据
            if (null == catalogJsonFromDB) {//数据库中也查询不到
                //TODO 处理缓存穿透问题
                //缓存一个空对象 60s有效
                Map<String, List<Catalog2Vo>> nullCatalogJson = new HashMap<>();
                nullCatalogJson.put("1000000",Catalog2Vo.getNullCatalog2Vo());
                log.info("缓存穿透");
                redisTemplate.opsForValue().set(
                        "catalogJson",
                        nullCatalogJson,//缓存一个空对象
                        60,
                        TimeUnit.MINUTES);
                return nullCatalogJson;//返回一个空对象

            }
            //TODO 将查询到的数据缓存到redis中 在查库操作方法中进行 要确保在synchronized锁中进行
            return catalogJsonFromDB;//返回从数据库中查询到的数据
        }

        String toJSONString = JSON.toJSONString(jsonFromRedis);

        //缓存中查询到数据直接返回
        log.info("从缓存中获取到数据");
        Map<String, List<Catalog2Vo>> catalogJson = JSON.parseObject(
                toJSONString,
                new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
        return catalogJson;
    }


    /**
     * 从数据库中获取数据 TODO 使用Redisson
     * TODO 缓存中的数据和数据库中的数据保存数据一致性？(缓存多数应用在读多写少的业务情况下)
     * 1）双写模式(在数据库中进行了写操作 同时同步写到缓存中)
     * 2）失效模式(在数据库中进行了写操作 将缓存中的数据及时的进行删除 下次进行查询就从数据库中获取最新的数据)
     * 以上方法并发情况下仍然无法完全解决数据一致性问题
     *
     * 可以使用读写锁(读读情况下相当于无锁)
     * 使用canal（alibaba的解决缓存数据一致性的工具 canal将自己伪装成mysql的从库 实时将binlog中的写操作更新执行到redis中）订阅数据库的binlog
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedissonLock(){
        RLock lock = redissonClient.getLock("catalogJson-lock");//指定分布式锁的名字 锁的名字决定锁的粒度
        lock.lock();//加锁
        log.info("使用Redisson 加锁成功");
        Map<String, List<Catalog2Vo>> catalogJsonFromDB;
        try {
            catalogJsonFromDB = getCatalogJsonFromDB();//执行查库业务代码
        } finally {
            lock.unlock();//解锁
            log.info("使用Redisson 解锁");
        }
        return catalogJsonFromDB;

    }

    /**
     * 从数据库中获取数据 使用redis分布式锁
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        //上redis分布式锁SET Key Value NX EX Second
        //TODO 原子性上锁
        String uuid = UUID.randomUUID().toString();
        Boolean isRedisLock = redisTemplate.opsForValue().setIfAbsent(//上锁的同时设置锁的expireTime 过期时间 避免并发情况下造成死锁
                "redisLock",
                uuid,
                300,
                TimeUnit.SECONDS);
        if (isRedisLock) {//判断是否加锁成功
            log.info("获取redis分布式锁成功");
            //若加锁成功则进行后续操作 加锁成功后也同时要把锁进行删除
            Map<String, List<Catalog2Vo>> catalogJsonFromDB;
            try {
                catalogJsonFromDB = getCatalogJsonFromDB();//执行查库业务代码
            } finally {
                //TODO 原子性解锁 使用lua脚本
                String lua = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                redisTemplate.execute(
                        new DefaultRedisScript<Long>(lua, Long.class),//脚本 并且指定返回值类型Long 【1删除成功 0删除失败】
                        Arrays.asList("redisLock"),//key
                        uuid);//value
                log.info("lua脚本释放redis分布式锁");
            }
            return catalogJsonFromDB;
        } else {
            //若加锁不成功则重复获取锁(自旋)
            try {
                Thread.sleep(10000);//休眠10s
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();
        }
    }

    /**
     * 抽取方法 从数据库中查询数据
     * 后续的线程拿到锁后要查看是否前面的线程已经将数据查询到并缓存到redis中
     * 将查询到的数据缓存到redis中的操作同样要在synchronized锁中进行
     * @return
     */
    private Map<String, List<Catalog2Vo>> getCatalogJsonFromDB() {
        //TODO 后续的线程拿到锁后要查看是否前面的线程已经将数据查询到并缓存到redis中
        Object jsonFromRedis = redisTemplate.opsForValue().get("catalogJson");
        if (null != jsonFromRedis) {//若redis中已经缓存了数据则直接返回数据 不用进行后续的数据库查询操作
            log.info("从缓存中获取到数据");
            String toJSONString = JSON.toJSONString(jsonFromRedis);
            Map<String, List<Catalog2Vo>> catalogJson = JSON.parseObject(
                    toJSONString,
                    new TypeReference<Map<String, List<Catalog2Vo>>>() {
                    });
            return catalogJson;
        }
        log.info("从数据库中获取到数据");

        //1、TODO 查出所有分类(只需要查询一次数据库 将三级分类的数据全部查询出来 抽取出一个公共的方法 进行二级分类&三级分类菜单的查询)
        List<CategoryEntity> categoryEntityList = this.list(null);//查出三级分类菜单

        //1、1）查出所有一级分类
        List<CategoryEntity> level1Categories = getLevel1Categories();
        if(level1Categories == null){//查询一级缓存的非空判断
            return null;
        }
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

        //TODO 将查询到的数据缓存到redis中的操作同样要在锁中进行
        redisTemplate.opsForValue().set("catalogJson", parentCid, 30, TimeUnit.MINUTES);//缓存到redis中 30分钟失效
        log.info("将数据缓存到redis中");
        return parentCid;//返回数据中查询好的结果数据
    }

    /**
     * 从数据库中获取数据 使用本地锁synchronized（this）无法完美解决分布式情况下的并发问题
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithThisLock() {

        //TODO 分布式情况下本地锁无法满足 要使用分布式锁 后续会进行优化
        synchronized (this) {//TODO 加本地锁解决缓存穿透问题 给查询数据库的操作加上synchronized锁 锁对象使用this（SpringBoot所有容器组件都是单例的）

            //TODO 后续的线程拿到锁后要查看是否前面的线程已经将数据查询到并缓存到redis中
            return getCatalogJsonFromDB();
        }
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


    /**
     * 修改 同时修改pms_category_brand_relation分类和品牌管理的关联表
     * @param category
     */
    @CacheEvict(cacheNames = {"Categories"},key = "'getLevel1Categories'")//TODO 以及同时更新缓存（数据一致性之缓存失效方式）
    @Transactional
    @Override
    public void updateByIdWithBrandRelationAndCache(CategoryEntity category) {
        //更新分类数据
        this.updateById(category);
        //更新pms_category_brand_relation分类和品牌管理的关联表
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());

        //及时清除缓存
        redisTemplate.delete("catalogJson");

    }

}
