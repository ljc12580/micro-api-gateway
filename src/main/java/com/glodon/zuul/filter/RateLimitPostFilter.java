package com.glodon.zuul.filter;

import com.glodon.zuul.config.RateLimitKeyGenerator;
import com.glodon.zuul.config.RateLimiter;
import com.glodon.zuul.properties.RateLimitProperties;
import com.glodon.zuul.utils.RateLimitUtils;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

import static com.glodon.zuul.constant.RateLimitConstants.REQUEST_START_TIME;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;

/**
 * Created by liuqc-b on 2018/7/27.
 */
public class RateLimitPostFilter extends AbstractRateLimitFilter {

    private final RateLimitProperties properties;
    private final RateLimiter rateLimiter;
    private final RateLimitKeyGenerator rateLimitKeyGenerator;

    public RateLimitPostFilter(RateLimitProperties properties, RouteLocator routeLocator,
                               UrlPathHelper urlPathHelper, RateLimiter rateLimiter,
                               RateLimitKeyGenerator rateLimitKeyGenerator, RateLimitUtils rateLimitUtils) {
        super(properties, routeLocator, urlPathHelper, rateLimitUtils);
        this.properties = properties;
        this.rateLimiter = rateLimiter;
        this.rateLimitKeyGenerator = rateLimitKeyGenerator;
    }

    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return properties.getPostFilterOrder();
    }

    @Override
    public boolean shouldFilter() {
        return super.shouldFilter() && getRequestStartTime() != null;
    }

    private Long getRequestStartTime() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        final HttpServletRequest request = ctx.getRequest();
        return (Long) request.getAttribute(REQUEST_START_TIME);
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        Route route = route(request);

        policy(route, request).forEach(policy -> {
            Long requestTime = System.currentTimeMillis() - getRequestStartTime();
            String key = rateLimitKeyGenerator.key(request, route, policy);
            rateLimiter.consume(policy, key, requestTime > 0 ? requestTime : 1);
        });

        return null;
    }
}

