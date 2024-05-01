package ru.nsu.digitallibrary.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages
        = "ru.nsu.digitallibrary.repository.elasticsearch")
@ComponentScan(basePackages = {"ru.nsu.digitallibrary"})
public class ElasticSearchConfig {

//    @Bean
//    public RestHighLevelClient elasticsearchClient() {
//
//        RestClientBuilder builder = RestClient.builder(
//                        new HttpHost("localhost", 9200))
//                .setRequestConfigCallback(
//                        requestConfigBuilder -> requestConfigBuilder
//                                .setConnectionRequestTimeout(0));
//
//        return new RestHighLevelClient(builder);
//    }
//
//    @Bean
//    public ElasticsearchRestTemplate elasticsearchRestTemplate(RestHighLevelClient elasticsearchClient) {
//        return new ElasticsearchRestTemplate(elasticsearchClient);
//    }

}
