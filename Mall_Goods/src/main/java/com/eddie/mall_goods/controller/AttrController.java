package com.eddie.mall_goods.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.eddie.mall_goods.entity.ProductAttrValueEntity;
import com.eddie.mall_goods.service.ProductAttrValueService;
import com.eddie.mall_goods.vo.AttrRespVo;
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
    ProductAttrValueService productAttrValueService;

    @Autowired
    private AttrService attrService;

    // /product/attr/base/listforspu/{spuId}
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId") Long spuId){

        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrListForSpu(spuId);

        return R.ok().put("data",entities);
    }


    @GetMapping("/{attrType}/list/{catelogId}")
    public R getBaseAttrList(
            @RequestParam Map<String, Object> params
            ,@PathVariable("catelogId") Long catelogId
            ,@PathVariable("attrType") String attrType){
        PageUtils page = attrService.getBaseAttrPage(params,catelogId,attrType);
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
		AttrRespVo respVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", respVo);
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
    public R update(@RequestBody AttrVo attrVo){
		attrService.updateAttr(attrVo);
        return R.ok();
    }

    ///product/attr/update/{spuId}
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){

        productAttrValueService.updateSpuAttr(spuId,entities);

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
