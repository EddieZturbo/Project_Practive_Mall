package com.eddie.mall_search.service;


import com.eddie.mall_search.vo.SearchParam;
import com.eddie.mall_search.vo.SearchResult;

public interface MallSearchService {

    /**
     * @param param 检索的所有参数
     * @return  返回检索的结果，里面包含页面需要的所有信息
     */
    SearchResult search(SearchParam param);
}
