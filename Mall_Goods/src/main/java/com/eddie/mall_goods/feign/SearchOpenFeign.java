package com.eddie.mall_goods.feign;

import com.eddie.common.es.SkuEsModel;
import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

/**
 @author EddieZhang
 @create 2023-01-26 7:16 PM
 */
@FeignClient("cloud-mall-search")
public interface SearchOpenFeign {
    /**
     * 远程调用ES Search检索服务 上架goods
     * @param skuEsModelList
     * @return
     */
    @PostMapping("/search/save/goods")
    public R saveGoods(@RequestBody List<SkuEsModel> skuEsModelList);

}
