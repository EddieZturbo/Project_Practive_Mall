package com.eddie.mall_search.service.impl;

import com.alibaba.fastjson.JSON;
import com.eddie.common.es.SkuEsModel;
import com.eddie.mall_search.config.ElasticSearchConfig;
import com.eddie.mall_search.constant.ESConstant;
import com.eddie.mall_search.service.GoodsSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 @author EddieZhang
 @create 2023-01-26 5:16 PM
 */
@Service
@Slf4j
public class GoodsSaveServiceImpl implements GoodsSaveService {
    @Autowired
    private ElasticSearchConfig elasticSearchConfig;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Boolean esGoodsUp(List<SkuEsModel> skuEsModelList) throws IOException {
        //保存数据到ElasticSearch中
        //1)在es中建立index goods建立好映射关系   new_product_mapping.json
        //2）将数据保存到ES中（bulk 批量进行保存）
        //public final BulkResponse bulk(BulkRequest bulkRequest, RequestOptions options)
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsMd : skuEsModelList) {
            //构造ES保存请求
            IndexRequest indexRequest = new IndexRequest(ESConstant.GOODS_INDEX);//指定储存到ES中的index
            indexRequest.id(skuEsMd.getSkuId().toString());//将SkuId作为储存到ES中的id
            String jsonString = JSON.toJSONString(skuEsMd);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);//将所有的ES保存请求add到BulkRequest中进行批量保存
        }
        BulkResponse responses = client.bulk(bulkRequest, elasticSearchConfig.COMMON_OPTIONS);

        //TODO 若对ES保存时出现failure 继续进行处理
        BulkItemResponse[] items = responses.getItems();
        List<String> errorGoodsIds = Arrays.stream(items)
                .filter(errorItem -> {
                    return errorItem.isFailed();
                })
                .map(item -> {
                    return item.getId();
                })
                .collect(Collectors.toList());
        if (errorGoodsIds.size() > 0) {
            log.error("ES保存数据出错 出错的商品id:  {}", errorGoodsIds);
        }

        boolean hasFailures = responses.hasFailures();
        return hasFailures;//若有出错则返回true
    }
}
