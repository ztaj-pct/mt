package com.pct.device.feing.config;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
//Aamir

//set header,other data in req bfor sending via feign client
public class AuthTokenInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
    	
    	 HttpServletRequest request =
                 ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
         template.header("Authorization", request.getHeader("Authorization"));                

    }
}
