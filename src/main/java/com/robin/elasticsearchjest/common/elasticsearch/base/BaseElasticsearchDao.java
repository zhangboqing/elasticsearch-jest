package com.robin.elasticsearchjest.common.elasticsearch.base;

import com.robin.elasticsearchjest.common.elasticsearch.annotation.ESDocument;
import com.robin.elasticsearchjest.common.elasticsearch.annotation.ESId;
import com.robin.elasticsearchjest.common.exception.ElasticsearchException;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author zhangboqing
 * @date 2019/12/10
 */
@Slf4j
public abstract class BaseElasticsearchDao<T> implements InitializingBean {

    @Autowired
    protected ElasticsearchUtils elasticsearchUtils;
    @Autowired
    protected JestClient client;

    /**
     * 索引名称
     */
    protected String indexName;
    /**
     * ID字段
     */
    protected Field idField;
    /**
     * T对应的类型Class
     */
    protected Class<T> genericClass;

    public BaseElasticsearchDao() {
        Class<T> beanClass = (Class<T>) GenericTypeResolver.resolveTypeArgument(this.getClass(), BaseElasticsearchDao.class);
        this.genericClass = beanClass;

        ESDocument esDocument = AnnotationUtils.findAnnotation(beanClass, ESDocument.class);
        this.indexName = esDocument.indexName();

        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            ESId esId = declaredField.getAnnotation(ESId.class);
            if (esId != null) {
                this.idField = declaredField;
                idField.setAccessible(true);
                break;
            }
        }
    }


    /**
     * 保存或更新文档数据
     *
     * @param list 文档数据集合
     */
    public void saveOrUpdate(List<T> list) {

        list.forEach(genericInstance -> {
            Index index = new Index.Builder(genericInstance).index(indexName).type(indexName).id(getIdValue(genericInstance)).build();
            try {
                JestResult result = client.execute(index);
                log.info(" saveOrUpdate responseCode: {},errorMessage: {}", result.getResponseCode(),result.getErrorMessage());
            } catch (IOException e) {
                e.printStackTrace();
                log.error("elasticsearch insert error", e);
            }
        });

    }

    /**
     * 多条件等值查询查询
     * 当key为分词字段，自动进行全文搜索
     * @param fieldNameToQueryValue key:fieldName;value:queryValue
     * @return
     */
    public List<T> searchByEq(Map<String,String> fieldNameToQueryValue) {
        ESPageResult<T> search = searchByEq(fieldNameToQueryValue, null, null);
        return search != null ? search.getResults() : null;
    }

    /**
     * 多条件等值查询查询
     * 当key为分词字段，自动进行全文搜索
     * @param fieldNameToQueryValue key:fieldName;value:queryValue
     * @return
     */
    public ESPageResult<T> searchByEq(Map<String,String> fieldNameToQueryValue, ESPageRequest esPageRequest, ESSort esSort) {
        Assert.notEmpty(fieldNameToQueryValue,"fieldNameToQueryValue is null");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (Map.Entry<String, String> entry : fieldNameToQueryValue.entrySet()) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery(entry.getKey(),entry.getValue()));
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        return search(searchSourceBuilder, esPageRequest, esSort);
    }



    /**
     * 多条件like查询查询
     * 支持*或？，*多个字符，？单个字符
     * @param fieldNameToQueryValue key:fieldName;value:queryValue
     * @return
     */
    public List<T> searchByLike(Map<String,String> fieldNameToQueryValue) {
        ESPageResult<T> search = searchByLike(fieldNameToQueryValue, null, null);
        return search != null ? search.getResults() : null;
    }


    /**
     * 多条件like查询查询
     * 支持*或？，*多个字符，？单个字符
     * @param fieldNameToQueryValue key:fieldName;value:queryValue
     * @return
     */
    public ESPageResult<T> searchByLike(Map<String,String> fieldNameToQueryValue, ESPageRequest esPageRequest, ESSort esSort) {
        Assert.notEmpty(fieldNameToQueryValue,"fieldNameToQueryValue is null");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (Map.Entry<String, String> entry : fieldNameToQueryValue.entrySet()) {
            boolQueryBuilder.filter(QueryBuilders.wildcardQuery(entry.getKey(),entry.getValue()));
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        return search(searchSourceBuilder, esPageRequest, esSort);
    }


    /**
     * 搜索文档，根据指定的搜索条件
     *
     * @param searchSourceBuilder
     * @return
     */
    public List<T> search(SearchSourceBuilder searchSourceBuilder) {
        ESPageResult search = search(searchSourceBuilder, null, null);
        return search != null ? search.getResults() : null;
    }

    /**
     * 分页排序搜索文档，根据指定的搜索条件
     *
     * @param searchSourceBuilder
     * @param esPageRequest       分页
     * @param esSort              排序
     * @return
     */
    public ESPageResult<T> search(SearchSourceBuilder searchSourceBuilder, ESPageRequest esPageRequest, ESSort esSort) {

        // 搜索
        Assert.notNull(searchSourceBuilder, "searchSourceBuilder is null");
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);

        // 分页
        if (esPageRequest != null) {
            searchSourceBuilder.from(esPageRequest.getPageNo() - 1);
            searchSourceBuilder.size(esPageRequest.getSize());
        }

        // 排序
        if (esSort != null) {
            List<ESSort.ESOrder> orders = esSort.orders;
            if (!CollectionUtils.isEmpty(orders)) {
                orders.forEach(esOrder -> searchSourceBuilder.sort(esOrder.getProperty(), esOrder.getDirection()));
            }
        }

        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex(indexName)
                .addType(indexName)
                .build();

        SearchResult result = null;
        try {
            result = client.execute(search);
            log.info(" updateRequest responseCode: {},errorMessage: {}", result.getResponseCode(),result.getErrorMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (result == null) {
            return null;
        }

        List<T> genericInstanceList = result.getSourceAsObjectList(genericClass, false);


        ESPageResult<T> pageResult = new ESPageResult<>(
                result.getTotal(),
                esPageRequest != null ? esPageRequest.getPageNo() : -1,
                esPageRequest != null ? esPageRequest.getSize() : -1,
                genericInstanceList);
        return pageResult;

    }



    /**
     * 删除操作
     * genericInstance = null,删除所有
     *
     * @param genericInstance 被删除的实例对象
     */
    public void delete(T genericInstance) {
        if (ObjectUtils.isEmpty(genericInstance)) {
            // 如果对象为空，则删除全量
            searchList().forEach(result -> {
                delete(getIdValue(result));
            });
        }
        delete(getIdValue(genericInstance));
    }

    /**
     * 删除操作
     * @param id 文档ID
     */
    public void delete(String id) {
        elasticsearchUtils.deleteRequest(indexName, id);
    }


    /**
     * ============================================================================================================
     *                                                  私有方法
     * ============================================================================================================
     * */

    private List<T> searchList() {
        SearchResult searchResult = elasticsearchUtils.search(indexName);
        List<T> genericInstanceList = searchResult.getSourceAsObjectList(genericClass, false);
        return genericInstanceList;
    }

    /**
     * 获取当前操作的genericInstance的主键ID
     *
     * @param genericInstance 实例对象
     * @return 返回主键ID值
     */
    private String getIdValue(T genericInstance) {
        try {
            Object idValue = idField.get(genericInstance);
            return idValue == null ? null : idValue.toString();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ESDocument esDocument = AnnotationUtils.findAnnotation(genericClass, ESDocument.class);
        if (esDocument == null) {
            throw new ElasticsearchException("ESDocument注解未指定");
        }
        String indexName = esDocument.indexName();
        if (StringUtils.isEmpty(indexName)) {
            throw new ElasticsearchException("indexName未指定");
        }
        int shards = esDocument.shards();
        int replicas = esDocument.replicas();

        if (shards == 0 || replicas == 0) {
            elasticsearchUtils.createIndexRequest(indexName);
        } else {
            elasticsearchUtils.createIndexRequest(indexName, shards, replicas);
        }
        elasticsearchUtils.putMappingRequest(indexName, genericClass);
    }
}
