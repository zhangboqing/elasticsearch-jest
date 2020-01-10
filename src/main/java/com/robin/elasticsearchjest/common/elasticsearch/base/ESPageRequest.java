package com.robin.elasticsearchjest.common.elasticsearch.base;

import lombok.Data;

/**
 * 分页
 * @author zhangboqing
 * @date 2019/12/29
 */
@Data
public class ESPageRequest {
    /** 当前页 */
    private final int pageNo;
    /** 每页大小 */
    private final int size;


    private ESPageRequest(int pageNo, int size) {

        if (pageNo < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero!");
        }

        if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one!");
        }

        this.pageNo = pageNo;
        this.size = size;
    }

    public static ESPageRequest builder(int pageNo,int size) {
        return new ESPageRequest(pageNo,size);
    }

}

