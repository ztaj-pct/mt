package com.pct.device.config;

import java.net.URI;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
public class ElasticConfig {

	@Value("${spring.elasticsearch.rest.uris}")
	String url;

	@Value("${spring.elasticsearch.rest.username}")
	String elasticSearchUsername;

	@Value("${spring.elasticsearch.rest.password}")
	String elasticSearchPassword;
	
	@Value("${client.configration.url}")
	String clientConfigUrl;

	
	@Bean
    public RestHighLevelClient client() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder clientConfiguration 
            = ClientConfiguration.builder()
                .connectedTo(clientConfigUrl);
        URI uri = URI.create(url);
        boolean isSsl = "https".equals(uri.getScheme());
		if (isSsl) {
			clientConfiguration.usingSsl();
		}
		clientConfiguration.withBasicAuth(elasticSearchUsername, elasticSearchPassword).withSocketTimeout(50000);
		RestHighLevelClient rs = RestClients.create(clientConfiguration.build()).rest();
        return rs;
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(client());
    }
}