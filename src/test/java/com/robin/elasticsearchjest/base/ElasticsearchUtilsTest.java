package com.robin.elasticsearchjest.base;

import com.robin.elasticsearchjest.common.elasticsearch.base.ElasticsearchUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author zhangboqing
 * @Date 2020-01-07
 */

@SpringBootTest
class ElasticsearchUtilsTest {

    @Autowired
    ElasticsearchUtils elasticsearchUtils;

    @Test
    void existIndex() {
        elasticsearchUtils.existIndex("goods");
    }
}