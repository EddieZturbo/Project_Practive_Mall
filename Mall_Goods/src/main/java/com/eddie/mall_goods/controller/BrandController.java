package com.eddie.mall_goods.controller;

import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.R;
import com.eddie.common.validation.SaveGroup;
import com.eddie.common.validation.UpdateStatusGroup;
import com.eddie.mall_goods.entity.BrandEntity;
import com.eddie.mall_goods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 品牌
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:57:47
 */
@RestController
@RequestMapping("mall_goods/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mall_goods:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("mall_goods:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mall_goods:brand:save")
    //TODO 要使后端的JSR303规范(注解方式)进行数据校验生效 必须在需要校验的参数上加上 @Validated注解 要想获取校验结果 必须紧跟BindingResult result对象
    public R save(@Validated(value = {SaveGroup.class})/*指定Validation分组 定义分组验证*/
                      @RequestBody BrandEntity brand/*BindingResult result 去掉此参数即将不处理参数校验异常 抛出去给到全局处理 */) {
//        if (result.hasErrors()) {//判断校验是否有错误
//            HashMap<String, String> map = new HashMap<>();
//            result.getFieldErrors().forEach((item) -> {
//                String errorName = item.getField();//错误字段的name
//                String errorMessage = item.getDefaultMessage();//错误的message
//                map.put(errorName,errorMessage);
//            });
//            return R.error(400,"提交的数据不合法").put("data",map);
//        } else {
            brandService.save(brand);
            return R.ok();
//        }
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mall_goods:brand:update")
    public R update(@Validated(value = {SaveGroup.class})/*指定Validation分组 定义分组验证*/
                        @RequestBody BrandEntity brand/*BindingResult result 去掉此参数即将不处理参数校验异常 抛出去给到全局处理 */) {
//        if (result.hasErrors()) {//判断校验是否有错误
//            HashMap<String, String> map = new HashMap<>();
//            result.getFieldErrors().forEach((item) -> {
//                String errorName = item.getField();//错误字段的name
//                String errorMessage = item.getDefaultMessage();//错误的message
//                map.put(errorName,errorMessage);
//            });
//            return R.error(400,"提交的数据不合法").put("data",map);
//        } else {
            brandService.updateById(brand);
            return R.ok();
//        }
    }

    /**
     * 修改status
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("mall_goods:brand:update")
    public R updateStatus(@Validated(value = {UpdateStatusGroup.class})/*指定Validation分组 定义分组验证*/
                              @RequestBody BrandEntity brand/*BindingResult result 去掉此参数即将不处理参数校验异常 抛出去给到全局处理 */) {
        brandService.updateById(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mall_goods:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
