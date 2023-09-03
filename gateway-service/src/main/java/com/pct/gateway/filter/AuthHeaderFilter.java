package com.pct.gateway.filter;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.collections4.Equator;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@Component
public class AuthHeaderFilter extends ZuulFilter {

    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;
    private static final String PRE_FILTER_TYPE = "pre";
    private static final String AUTH_HEADER = "Authorization";
    private static final List<String> USER_EXCLUDE_URL;
    private static final List<String> INTERNAL_EXCLUDE_URL;
    private static final List<String> SWAGGER_EXCLUDE_URL;

    static {
        USER_EXCLUDE_URL = new ArrayList<>();
        INTERNAL_EXCLUDE_URL = new ArrayList<>();
        SWAGGER_EXCLUDE_URL = new ArrayList<>();

        USER_EXCLUDE_URL.add("/user/token/login");
        USER_EXCLUDE_URL.add("/user/forgot-password");
        USER_EXCLUDE_URL.add("/user/forgot-password/otp");
        USER_EXCLUDE_URL.add("/user/reset-password");
        USER_EXCLUDE_URL.add("/device/uploadDevice");
        USER_EXCLUDE_URL.add("/device/download");
        USER_EXCLUDE_URL.add("/campaign/execution");

        //commented
      //  INTERNAL_EXCLUDE_URL.add("/device/core/");
        INTERNAL_EXCLUDE_URL.add("/user/core");
        //INTERNAL_EXCLUDE_URL.add("/user/username");
        INTERNAL_EXCLUDE_URL.add("/customer/core");
        INTERNAL_EXCLUDE_URL.add("/installation/core");
        INTERNAL_EXCLUDE_URL.add("/product/core");
        
        SWAGGER_EXCLUDE_URL.add("/user/v2/api-docs");
        SWAGGER_EXCLUDE_URL.add("/device/v2/api-docs");
        SWAGGER_EXCLUDE_URL.add("/device-command/v2/api-docs");
        SWAGGER_EXCLUDE_URL.add("/organisation/v2/api-docs");
        SWAGGER_EXCLUDE_URL.add("/installation/v2/api-docs");
        SWAGGER_EXCLUDE_URL.add("/installer/v2/api-docs");
        
        USER_EXCLUDE_URL.add("/azure/login");
        USER_EXCLUDE_URL.add("/app/login");
        USER_EXCLUDE_URL.add("/azure/logout");
        USER_EXCLUDE_URL.add("/app/logout");
    }

    @Autowired
    private EurekaClient eurekaClient;

    @Value("${service.organisation.serviceId}")
    private String companyServiceId;

    @Override
    public String filterType() {
        return PRE_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String fullUrl = request.getRequestURL().toString();
        String[] splitUrl = fullUrl.split("/");
        String urlToCheck = "";
        for (int i = 3; i < splitUrl.length; i++) {
            urlToCheck += "/" + splitUrl[i];
        }
        if (!IterableUtils.contains(USER_EXCLUDE_URL, urlToCheck, new ListEquator())
                && !IterableUtils.contains(INTERNAL_EXCLUDE_URL, urlToCheck, new ListEquator())
                && !IterableUtils.contains(SWAGGER_EXCLUDE_URL, urlToCheck, new ListEquator())) {
            if (request.getHeader(AUTH_HEADER) == null || request.getHeader(AUTH_HEADER).isEmpty()) {
                ctx.setResponseBody("Auth header not present");
                ctx.getResponse().setHeader("Content-Type", "text/plain;charset=UTF-8");
                ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
                ctx.setSendZuulResponse(false);
            }
        } else {
            if (IterableUtils.contains(INTERNAL_EXCLUDE_URL, urlToCheck, new ListEquator())) {
                Application companyService = eurekaClient.getApplication(companyServiceId);
                InstanceInfo companyServiceInfo = companyService.getInstances().get(0);
                if (!companyServiceInfo.getIPAddr().equalsIgnoreCase(request.getRemoteAddr())) {
                    ctx.setResponseBody("Request not allowed from host");
                    ctx.getResponse().setHeader("Content-Type", "text/plain;charset=UTF-8");
                    ctx.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
                    ctx.setSendZuulResponse(false);
                }
            }
        }
        return null;
    }

    class ListEquator implements Equator<String> {
        @Override
        public boolean equate(String o1, String o2) {
            return o1.contains(o2);
        }

        @Override
        public int hash(String o) {
            return o.hashCode();
        }
    }
}

