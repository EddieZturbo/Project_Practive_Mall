package com.eddie.mall_goods.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.eddie.mall_goods.entity.BrandEntity;
import com.eddie.mall_goods.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.eddie.mall_goods.entity.CategoryBrandRelationEntity;
import com.eddie.mall_goods.service.CategoryBrandRelationService;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:57:46
 */
@RestController
@RequestMapping("mall_goods/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * TODO ①controller层：处理请求 接收和校验数据
     * ②service层就收controller层传来的数据 进行业务逻辑处理
     * ③controller接收service处理完的数据 封装成页面指定的VO
     * @param catId
     * @return
     */
    @GetMapping("/brands/list")
    public R relationBrandList(@RequestParam(value = "catId") Long catId){
        List<BrandEntity> brandEntities = categoryBrandRelationService.getBrandsByCatId(catId);

        List<BrandVo> brandVos = brandEntities.stream()
                .map((item) -> {
                    BrandVo vo = new BrandVo();
                    vo.setBrandName(item.getName());
                    vo.setBrandId(item.getBrandId());
                    return vo;
                })
                .collect(Collectors.toList());
        return R.ok().put("data",brandVos);

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mall_goods:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("mall_goods:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mall_goods:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.save(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mall_goods:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mall_goods:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
