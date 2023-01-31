package com.eddie.mall_goods.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 @author EddieZhang
 @create 2023-01-28 9:07 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catalog2Vo implements Serializable {
    /**
     * 一级父分类的id
     */
    private String catalog1Id;

    /**
     * 三级子分类
     */
    private List<Catalog3Vo> catalog3List;

    private String id;

    private String name;

    /**
     * 获取一个空对象实例 解决redis的缓存穿透问题(缓存一个空对象)
     * @return
     */
    public static List<Catalog2Vo> getNullCatalog2Vo(){
        Catalog2Vo catalog2Vo = new Catalog2Vo();
        catalog2Vo.setCatalog1Id("10000");
        List<Catalog3Vo> catalog3Vos = new ArrayList<>();
        catalog3Vos.add(Catalog3Vo.getNullCatalog3Vo());
        catalog2Vo.setCatalog3List(catalog3Vos);
        catalog2Vo.setId("10000");
        catalog2Vo.setName("空对象");

        List<Catalog2Vo> catalog2VoList = new ArrayList<>();
        catalog2VoList.add(catalog2Vo);
        return catalog2VoList;
    }


    /**
     * 三级分类vo
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catalog3Vo implements Serializable{

        /**
         * 父分类、二级分类id
         */
        private String catalog2Id;

        private String id;

        private String name;

        public static Catalog3Vo getNullCatalog3Vo(){
            Catalog3Vo catalog3Vo = new Catalog3Vo();
            catalog3Vo.setCatalog2Id("100000");
            catalog3Vo.setId("100000");
            catalog3Vo.setName("空对象");
            return catalog3Vo;
        }



    }

}
