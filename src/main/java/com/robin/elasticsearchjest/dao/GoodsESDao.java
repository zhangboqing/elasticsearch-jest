package com.robin.elasticsearchjest.dao;

import com.robin.elasticsearchjest.common.elasticsearch.base.BaseElasticsearchDao;
import com.robin.elasticsearchjest.common.elasticsearch.base.ESPageRequest;
import com.robin.elasticsearchjest.common.elasticsearch.base.ESPageResult;
import com.robin.elasticsearchjest.common.elasticsearch.base.ESSort;
import com.robin.elasticsearchjest.model.GoodsESEntity;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangboqing
 * @date 2019/12/10
 */
@Component
public class GoodsESDao<T extends GoodsESEntity> extends BaseElasticsearchDao<GoodsESEntity> {


    /**
     * 全文搜索查询
     * @param queryName
     * @return
     */
    public List<GoodsESEntity> findListByAnalysisForGroupData(String queryName) {
        Map<String,String> map = new HashMap<>();
        map.put("groupData","保时捷跑车V20");
        return searchByEq(map);
    }

    /**
     * 多条件等值查询查询
     * @return
     */
    public List<GoodsESEntity> findListByEq() {
        Map<String,String> map = new HashMap<>();
        map .put("goodsName","保时捷跑车V10");
        map .put("goodBrand","国际2");
        return searchByEq(map);
    }

    /**
     * 多条件like查询查询
     * 支持*或？，*多个字符，？单个字符
     * @return
     */
    public List<GoodsESEntity> findListByLike() {
        Map<String,String> map = new HashMap<>();
        map .put("goodsName","保时捷跑车V1?");
        map .put("goodBrand","国际1");
        return searchByLike(map);
    }

    /**
     * 全文搜索查询分页排序
     * @param queryName
     * @return
     * QueryBuilders.boolQuery()
     */
    public ESPageResult findList(String queryName) {

        Map<String,String> map = new HashMap<>();
        map.put("groupData","保时捷跑车V20");

        // 分页
        ESPageRequest esPageRequest = ESPageRequest.builder(1, 2);

        // 排序
        ESSort esSort = ESSort.builder(SortOrder.ASC, "goodsId");

        ESPageResult<GoodsESEntity> search = searchByEq(map, esPageRequest, esSort);
        return search;
    }
}


