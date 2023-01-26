package com.eddie.common.to;

import lombok.Data;

/**
 @author EddieZhang
 @create 2023-01-26 4:16 PM
 */
@Data
public class SkuHasStockTo {
    private Long skuId;
    private Boolean hasStock;//是否有库存
}
