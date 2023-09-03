package com.pct.gateway.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Component
@Primary
@EnableAutoConfiguration
@EnableSwagger2
@Configuration
public class SwaggerConfig implements SwaggerResourcesProvider {

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<SwaggerResource>();
        resources.add(swaggerResource("auth-service", "/user/v2/api-docs", "2.0"));
        resources.add(swaggerResource("device-service", "/device/v2/api-docs", "2.0"));
        resources.add(swaggerResource("organisation-service", "/organisation/v2/api-docs", "2.0"));
        resources.add(swaggerResource("device-command-service", "/device-command/v2/api-docs", "2.0"));
        resources.add(swaggerResource("installer-service", "/installation/v2/api-docs", "2.0"));
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location, String version) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion(version);
        return swaggerResource;
    }
}