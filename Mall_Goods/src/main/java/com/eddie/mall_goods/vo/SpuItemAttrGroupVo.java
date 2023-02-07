package com.eddie.mall_goods.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class SpuItemAttrGroupVo {

    private String groupName;

    private List<Attr> attrs;

}
