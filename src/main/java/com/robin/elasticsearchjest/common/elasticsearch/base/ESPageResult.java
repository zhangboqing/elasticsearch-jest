package com.robin.elasticsearchjest.common.elasticsearch.base;

import lombok.Data;

import java.util.List;

/**
 * @Author zhangboqing
 * @Date 2019-12-30
 * 分页结果对象
 */
@Data
public class ESPageResult<T> {
    /** 总记录数 */
    private final long total;
    private final int pageNo;
    private final int pageSize;
    private List<T> results;

    public ESPageResult(long total, int pageNo, int pageSize, List<T> results) {
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.results = results;
    }
}
