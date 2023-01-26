package com.eddie.mall_ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.eddie.common.to.SkuHasStockTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.eddie.mall_ware.entity.WareSkuEntity;
import com.eddie.mall_ware.service.WareSkuService;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.R;


/**
 * 商品库存
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-16 21:20:00
 */
@RestController
@RequestMapping("mall_ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 根据skuId查看是否有库存
     * @param skuIds
     * @return
     */
    @PostMapping("/hasStock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockTo> skuHasStockToList = wareSkuService.skuHasStock(skuIds);
        return R.ok().put("data",skuHasStockToList);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mall_ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("mall_ware:waresku:info")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mall_ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mall_ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mall_ware:waresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
