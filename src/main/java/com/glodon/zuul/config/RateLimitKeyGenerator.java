package com.glodon.zuul.config;

import com.glodon.zuul.properties.RateLimitProperties;

import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by liuqc-b on 2018/7/24.
 */

public interface RateLimitKeyGenerator {

    /**
     * Returns a key based on {@link HttpServletRequest}, {@link Route} and
     * {@link RateLimitProperties.Policy}
     *
     * @param request The {@link HttpServletRequest}
     * @param route   The {@link Route}
     * @param policy  The {@link RateLimitProperties.Policy}
     * @return Generated key
     */
    String key(HttpServletRequest request, Route route, RateLimitProperties.Policy policy);
}

