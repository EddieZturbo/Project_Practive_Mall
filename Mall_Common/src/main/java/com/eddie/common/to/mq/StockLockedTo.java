package com.eddie.common.to.mq;

import lombok.Data;


@Data
public class StockLockedTo {

    /** 库存工作单的id **/
    private Long id;

    /** 工作单详情的所有信息 **/
    private com.eddie.common.to.mq.StockDetailTo detailTo;
}
