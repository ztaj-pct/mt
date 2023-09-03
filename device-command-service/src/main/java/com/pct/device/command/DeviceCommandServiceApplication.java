package com.pct.device.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EntityScan(basePackages = { "com.pct.device.command","com.pct.device.common","com.pct.common"})
@ComponentScan(basePackages = { "com.pct.device.command" ,"com.pct.device.command.serializer","com.pct.device.common","com.pct.parser.controller","com.pct.parser.engine","com.pct.parser","com.pct.device.common.repository","com.pct.common"})
@EnableJpaRepositories(basePackages = { "com.pct.device.command","com.pct.device.common.repository","com.pct.common" })
@EnableTransactionManagement
@EnableEurekaClient
@EnableScheduling
public class DeviceCommandServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeviceCommandServiceApplication.class, args);
	}

	
	@Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
