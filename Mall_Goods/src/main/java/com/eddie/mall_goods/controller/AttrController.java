package com.eddie.mall_goods.controller;

import java.util.Arrays;
import java.util.Map;

import com.eddie.mall_goods.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.eddie.mall_goods.entity.AttrEntity;
import com.eddie.mall_goods.service.AttrService;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.R;



/**
 * 商品属性
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:57:47
 */
@RestController
@RequestMapping("mall_goods/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;


    @GetMapping("/base/list/{catelogId}")
    public R getBaseAttrList(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId){
        PageUtils page = attrService.getBaseAttrPage(params,catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mall_goods:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("mall_goods:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrEntity attr = attrService.getById(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mall_goods:attr:save")
    public R save(@RequestBody AttrVo attrVo/*AttrVo（规格基本信息和关联关系信息）*/){
//		attrService.save(attr);
        attrService.saveAttrVo(attrVo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mall_goods:attr:update")
    public R update(@RequestBody AttrEntity attr){
		attrService.updateById(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mall_goods:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
