package pl.starchasers.mdpages.configutation

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ElasticsearchConfig {
    @Value("\${elasticsearch.host}")
    private val elasticsearchHost: String? = null

    @Value("\${elasticsearch.port}")
    private val elasticsearchPort: Int = 0

    @Bean(destroyMethod = "close")
    fun client(): RestHighLevelClient {
        return RestHighLevelClient(
            RestClient.builder(HttpHost(elasticsearchHost, elasticsearchPort))
        )
    }
}