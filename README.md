## 使用教程
#### 1）引入elasticsearch-jest依赖,并指定你的elasticsearch版本，默认5.5.3
```
    <dependency>
        <groupId>com.robin</groupId>
        <artifactId>elasticsearch-jest</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>org.elasticsearch</groupId>
        <artifactId>elasticsearch</artifactId>
        <version>${elasticsearch}</version>
    </dependency>
```    
#### 2）在application.yml配置文件中指定相关属性
```yaml
    data:
      elasticsearch:
        jest:
          # es集群节点  http://localhost:9200
          urls:
            - 'http://localhost:9200'
          # 设置连接es的用户名和密码
          account:
            username: elastic
            password: 123456
```
#### 3）定义es文档对象,需要使用相关的ES注解进行标注
```java
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ESDocument(indexName = "goods")
    public class GoodsESEntity {
        // 筛选条件包括：商品名称，品牌，规格，适用车型，商品编号，原厂编号
    
        /**
         * 主键,商品ID
         */
        @ESId
        @ESField(value = "goodsId",type = ESFieldType.Long)
        private Long goodsId;
    
        /**
         * 商品名称
         */
        @ESField(value = "goodsName",type = ESFieldType.Keyword)
        private String goodsName;
        /**
         * 品牌
         */
        @ESField(value = "goodBrand",type = ESFieldType.Keyword)
        private String goodBrand;
        /**
         * 规格
         */
        @ESField(value = "goodsSpec",type = ESFieldType.Keyword)
        private String goodsSpec;
        /**
         * 商品编号
         */
        @ESField(value = "goodsAccessoriesCode",type = ESFieldType.Keyword)
        private String goodsAccessoriesCode;
        /**
         * 原厂编号
         */
        @ESField(value = "goodsOriginalFactoryCode",type = ESFieldType.Keyword)
        private String goodsOriginalFactoryCode;
    
        /**
         * 复合字段，会被分词后存储
         */
        @ESField(value = "groupData",type = ESFieldType.Text,analyzer = "ik_smart")
        private String groupData;
    }
```
#### 4）定义es DAO数据库访问对象，需要继承BaseElasticsearchDao类（封装了基本的增删改查操作）
```java
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
            ESPageRequest esPageRequest = new ESPageRequest(1, 2);
    
            // 排序
            ESSort esSort = new ESSort(SortOrder.ASC,"goodsId");
    
            ESPageResult<GoodsESEntity> search = searchByEq(map, esPageRequest, esSort);
            return search;
        }
    }

```