package com.pct.device.version;

import java.time.Duration;
import java.util.TimeZone;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * @author Abhishek on 28/09/20
 */

@SpringBootApplication
@EnableEurekaClient
@EntityScan(basePackages = {"com.pct.device.version", "com.pct.common","com.pct.device.version.model"})
@ComponentScan(basePackages = {"com.pct.device.version", "com.pct.common"})
//@EnableJpaRepositories(basePackages = {"com.pct.device.version", "com.pct.common"})
@EnableScheduling
@EnableAsync
public class DeviceVersionApplication {

	@PostConstruct
	void init() {
	TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
    public static void main(String[] args) {

	    SpringApplication.run(DeviceVersionApplication.class, args);
    }
    @Bean(name = "asyncTaskExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsynchTaskThread-");
        executor.initialize();
        return executor;
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(150000))
                .setReadTimeout(Duration.ofMillis(150000))
                .build();
    }
    
}
