package com.robin.elasticsearchjest.common.elasticsearch.base;

import lombok.Builder;
import lombok.Data;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author zhangboqing
 * @Date 2019/12/29
 * 封装排序参数
 */
public class ESSort {

    /** 排序对象集合 */
    public final List<ESOrder> orders;
    private ESSort() {
        orders = new ArrayList<>();
    }

    private ESSort(SortOrder direction, String property) {
        orders = new ArrayList<>();
        add(direction,property);
    }

    public static ESSort builder() {
        return new ESSort();
    }

    public static ESSort builder(SortOrder direction, String property) {
        return new ESSort(direction,property);
    }

    /**
     * 追加排序字段
     * @param direction  排序方向
     * @param property  排序字段
     * @return
     */
    public ESSort add(SortOrder direction, String property) {

        Assert.notNull(direction, "direction must not be null!");
        Assert.hasText(property, "fieldName must not be empty!");

        orders.add(ESOrder.builder().direction(direction).property(property).build());
        return this;
    }


    @Builder
    @Data
    public static class ESOrder implements Serializable {

        /** 方向 */
        private final SortOrder direction;
        /** 字段名 */
        private final String property;

    }

}
