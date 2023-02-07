package com.eddie.mall_goods.vo;


import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class SkuItemSaleAttrVo {

    private Long attrId;

    private String attrName;

    private List<AttrValueWithSkuIdVo> attrValues;

}
