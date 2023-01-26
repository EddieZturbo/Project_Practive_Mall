package com.eddie.mall_search.controller;

import com.eddie.common.es.SkuEsModel;
import com.eddie.common.exception.BizCodeEnum;
import com.eddie.common.utils.R;
import com.eddie.mall_search.service.GoodsSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 @author EddieZhang
 @create 2023-01-26 5:13 PM
 */
@RestController
@Slf4j
@RequestMapping("/search/save")
public class ESSaveController {
    @Autowired
    private GoodsSaveService goodsSaveService;

    /**
     * 上架商品
     * @return
     */
    @PostMapping("/goods")
    public R saveGoods(@RequestBody List<SkuEsModel> skuEsModelList) {
        Boolean b = true;//有异常返回ture
        try {
            b = goodsSaveService.esGoodsUp(skuEsModelList);
        } catch (IOException e) {
            log.error("ElasticSearch保存 商品上架异常 {}", e);
            return R.error(BizCodeEnum.GOODS_UP_EXCEPTION.getCode(), BizCodeEnum.GOODS_UP_EXCEPTION.getMessage());
        }
        if (b) {//若ture则表示error
            return R.error(BizCodeEnum.GOODS_UP_EXCEPTION.getCode(), BizCodeEnum.GOODS_UP_EXCEPTION.getMessage());
        } else {
            return R.ok();
        }

    }

}
