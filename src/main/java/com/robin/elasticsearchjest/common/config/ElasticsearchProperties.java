package com.robin.elasticsearchjest.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


/**
 * @author zhangboqing
 * @date 2019/12/10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jest.data.elasticsearch")
public class ElasticsearchProperties {

    /**
     * 集群节点
     */
    @NotNull(message = "es地址不允许为空")
    private List<String> urls = new ArrayList();

    /**
     * 每个路由的最大连接数量
     */
    private Integer maxConnectPerRoute = 10;

    /**
     * 最大连接总数量
     */
    private Integer maxConnectTotal = 30;

    /**
     * 索引配置信息
     */
    private Index index = new Index();

    /**
     * 认证账户
     */
    private Account account = new Account();

    /**
     * Connection timeout. millisecond
     */
    private Integer connectionTimeout = 3000;

    /**
     * Read timeout. millisecond
     */
    private Integer readTimeout = 3000;

    /**
     * 索引配置信息
     */
    @Data
    public static class Index {

        /**
         * 分片数量
         */
        private Integer numberOfShards = 3;

        /**
         * 副本数量
         */
        private Integer numberOfReplicas = 2;

    }

    /**
     * 认证账户
     */
    @Data
    public static class Account {

        /**
         * 认证用户
         */
        private String username;

        /**
         * 认证密码
         */
        private String password;

    }

}
