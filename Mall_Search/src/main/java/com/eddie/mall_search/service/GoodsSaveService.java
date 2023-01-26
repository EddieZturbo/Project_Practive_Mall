package com.eddie.mall_search.service;

import com.eddie.common.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 @author EddieZhang
 @create 2023-01-26 5:16 PM
 */
public interface GoodsSaveService {
    Boolean esGoodsUp(List<SkuEsModel> skuEsModelList) throws IOException;
}
