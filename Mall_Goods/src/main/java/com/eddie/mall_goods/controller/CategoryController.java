package com.eddie.mall_goods.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eddie.mall_goods.entity.CategoryEntity;
import com.eddie.mall_goods.service.CategoryService;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.R;


/**
 * 商品三级分类
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:57:47
 */
@RestController
@RequestMapping("mall_goods/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 商品三级分类的层级列表
     */
    @RequestMapping("/list/listCategories")
    public R listCategories() {
        List<CategoryEntity> Data = categoryService.listCategories();
        return R.ok().put("listCategoriesData", Data);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mall_goods:category:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("mall_goods:category:info")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mall_goods:category:save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mall_goods:category:update")
    public R update(@RequestBody CategoryEntity category) {
        categoryService.updateById(category);

        return R.ok();
    }

    /**
     * 删除
     * @RequestBody 获取请求体 post有请求体 get没有请求体 因此需要将请求设置为post
     * 将前端传来的json数据转换成指定的对象形式
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mall_goods:category:delete")
    public R delete(@RequestBody Long[] catIds) {//完成逻辑删除
//		categoryService.removeByIds(Arrays.asList(catIds));
        categoryService.logicDeleteByIds(Arrays.asList(catIds));
        return R.ok().put("Delete Success!!",catIds);
    }

}
