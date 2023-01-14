package com.eddie.mall_goods.vo;

import com.eddie.mall_goods.entity.AttrAttrgroupRelationEntity;
import com.eddie.mall_goods.entity.AttrGroupEntity;
import lombok.Data;

@Data
public class AttrGroupRelationVo extends AttrAttrgroupRelationEntity{

    //"attrId":1,"attrGroupId":2
    private Long attrId;
    private Long attrGroupId;
}
