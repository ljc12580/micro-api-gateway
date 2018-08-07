package com.glodon.zuul.utils;

import lombok.AllArgsConstructor;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by liuqc-b on 2018/8/6.
 */
@AllArgsConstructor
@Component
public final class UrlUtils {
    private final RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper;

    public Route route(HttpServletRequest request) {
        String requestURI = urlPathHelper.getPathWithinApplication(request);
        return routeLocator.getMatchingRoute(requestURI);
    }
}
