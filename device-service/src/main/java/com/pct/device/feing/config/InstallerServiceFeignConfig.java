package com.pct.device.feing.config;

import org.springframework.context.annotation.Bean;

public class InstallerServiceFeignConfig {


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
