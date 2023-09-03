package com.pct.installer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"com.pct"})
@EntityScan(basePackages = {"com.pct.installer.entity", "com.pct.common.model"})
@ComponentScan(basePackages = {"com.pct.installer", "com.pct.common"})
@EnableJpaRepositories(basePackages = {"com.pct.installer.repository", "com.pct.common"})
@EnableEurekaClient
@EnableFeignClients
public class InstallerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstallerServiceApplication.class, args);
	}
	
	@Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
	
	
	

}
