package com.eddie.mall_goods.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.eddie.mall_goods.entity.SkuSaleAttrValueEntity;
import com.eddie.mall_goods.service.SkuSaleAttrValueService;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.R;



/**
 * sku销售属性&值
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:57:47
 */
@RestController
@RequestMapping("mall_goods/skusaleattrvalue")
public class SkuSaleAttrValueController {
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @GetMapping("stringList/{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId){
        List<String> skuSaleAttrValuesAsStringList = skuSaleAttrValueService.getSkuSaleAttrValuesAsStringList(skuId);
        return skuSaleAttrValuesAsStringList;
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mall_goods:skusaleattrvalue:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuSaleAttrValueService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("mall_goods:skusaleattrvalue:info")
    public R info(@PathVariable("id") Long id){
		SkuSaleAttrValueEntity skuSaleAttrValue = skuSaleAttrValueService.getById(id);

        return R.ok().put("skuSaleAttrValue", skuSaleAttrValue);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mall_goods:skusaleattrvalue:save")
    public R save(@RequestBody SkuSaleAttrValueEntity skuSaleAttrValue){
		skuSaleAttrValueService.save(skuSaleAttrValue);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mall_goods:skusaleattrvalue:update")
    public R update(@RequestBody SkuSaleAttrValueEntity skuSaleAttrValue){
		skuSaleAttrValueService.updateById(skuSaleAttrValue);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mall_goods:skusaleattrvalue:delete")
    public R delete(@RequestBody Long[] ids){
		skuSaleAttrValueService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
