package com.glodon.zuul.utils;

import com.glodon.zuul.properties.RateLimitProperties;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;


/**
 * Created by liuqc-b on 2018/7/24.
 */

@RequiredArgsConstructor
public final class RateLimitUtils {

    private static final String ANONYMOUS_USER = "anonymous";

    private static final String DEFAULT_TENANT = "default_tenant";


    public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final RateLimitProperties properties;

    public String getUser(HttpServletRequest request) {
        return request.getRemoteUser() != null ? request.getRemoteUser() : ANONYMOUS_USER;
    }
    public String getTenant(HttpServletRequest request) {
        String tenantId =request.getHeader("x-tenant-id");
        return tenantId != null ? tenantId : DEFAULT_TENANT;
    }

    public String getRemoteAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER);
        if (properties.isBehindProxy() && xForwardedFor != null) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

