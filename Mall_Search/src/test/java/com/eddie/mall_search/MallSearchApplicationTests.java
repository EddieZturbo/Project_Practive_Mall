package com.eddie.mall_search;

import com.eddie.mall_search.config.ElasticSearchConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallSearchApplicationTests {

    @Autowired
    private ElasticSearchConfig elasticSearchConfig;

    @Test
    public void contextLoads() {
        System.out.println(elasticSearchConfig);
    }

}
