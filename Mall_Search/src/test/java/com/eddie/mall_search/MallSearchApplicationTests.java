package com.eddie.mall_search;

import com.alibaba.fastjson.JSON;
import com.eddie.mall_search.config.ElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallSearchApplicationTests {

    @Autowired
    private ElasticSearchConfig elasticSearchConfig;

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void contextLoads() {
        System.out.println(elasticSearchConfig);
    }

    @Test
    public void test() throws IOException {

        IndexRequest indexRequest = new IndexRequest("users");//数据的Index
        indexRequest.id("1");//数据的id

        User user = new User();
        user.setName("Eddie");
        user.setGender('M');
        user.setAge(22);
        user.setMajor("Java");
        String jsonString = JSON.toJSONString(user);//定义一个对象并转成JSON字符串形式

        indexRequest.source(jsonString, XContentType.JSON);//要储存的数据

        IndexResponse index = client.index(indexRequest, elasticSearchConfig.COMMON_OPTIONS);//客户端进行data操作
        System.out.println(index);//输出执行响应结果
    }

    @Data
    class User{
        private String name;
        private Character gender;
        private Integer age;
        private String major;
    }

    @Test
    public void test2() throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//构造query条件
        sourceBuilder.query(QueryBuilders.matchAllQuery());

        SearchRequest searchRequest = new SearchRequest();//search操作
        searchRequest.indices("bank");//指定要search的index
        searchRequest.source(sourceBuilder);//_source操作

        SearchResponse searchResponse = client.search(searchRequest, elasticSearchConfig.COMMON_OPTIONS);//执行search操作
        //结果获取&分析
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit resultHit : searchHits) {
            String sourceAsString = resultHit.getSourceAsString();
            BankInfo bankInfo = JSON.parseObject(sourceAsString, BankInfo.class);
            System.out.println(bankInfo.toString());
            //MallSearchApplicationTests
            // .BankInfo(
            // account_number=1.0
            // , balance=39225.0
            // , firstname=Amber
            // , lastname=Duke
            // , age=32.0
            // , gender=M
            // , address=880 Holmes Lane
            // , employer=Pyrami
            // , email=amberduke@pyrami.com, city=Brogan, state=IL
            // )
            //...
        }
    }


    @Data
    static class BankInfo{
        private float account_number;
        private float balance;
        private String firstname;
        private String lastname;
        private float age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

}
