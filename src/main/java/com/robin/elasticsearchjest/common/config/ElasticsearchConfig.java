package com.robin.elasticsearchjest.common.config;

import com.robin.elasticsearchjest.common.elasticsearch.base.ElasticsearchUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author zhangboqing
 * @date 2019/12/10
 */
@ConditionalOnProperty(prefix = "data.elasticsearch.jest", value = "urls[0]")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@EnableConfigurationProperties(ElasticsearchProperties.class)
@Import(ElasticsearchUtils.class)
public class ElasticsearchConfig {

    private final ElasticsearchProperties elasticsearchProperties;


    @Bean
    @ConditionalOnMissingBean({ JestClient.class})
    public JestClient initializeJestClient() {

        HttpClientConfig clientConfig = new HttpClientConfig
                .Builder(elasticsearchProperties.getUrls())
                .multiThreaded(true)
                //Per default this implementation will create no more than 2 concurrent connections per given route
                .defaultMaxTotalConnectionPerRoute(elasticsearchProperties.getMaxConnectPerRoute())
                // and no more 20 connections in total
                .maxTotalConnection(elasticsearchProperties.getMaxConnectTotal())
                .defaultCredentials(elasticsearchProperties.getAccount().getUsername(), elasticsearchProperties.getAccount().getPassword())
                .readTimeout(elasticsearchProperties.getReadTimeout())
                .connTimeout(elasticsearchProperties.getConnectionTimeout())
                .build();

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(clientConfig);
        JestClient client = factory.getObject();
        return client;
    }

}

