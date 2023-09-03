package com.pct.installer.feign.config;

import feign.Contract;
import feign.codec.ErrorDecoder;

import org.springframework.context.annotation.Bean;

public class DeviceServiceFeignConfig {

    @Bean
    public AuthTokenInterceptor authRequestInterceptor() {
        return new AuthTokenInterceptor();
    }
    
////to check original exception from figen call
//    @Bean
//    public ErrorDecoder errorDecoder() {
//        return new RetreiveMessageErrorDecoder();
//    }

//    @Bean
//    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
//        return new BasicAuthRequestInterceptor("user", "password");
//    }

}
