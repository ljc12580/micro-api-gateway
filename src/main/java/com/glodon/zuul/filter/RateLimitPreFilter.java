package com.glodon.zuul.filter;

import com.glodon.zuul.config.RateLimitKeyGenerator;
import com.glodon.zuul.config.RateLimiter;
import com.glodon.zuul.model.Rate;
import com.glodon.zuul.properties.RateLimitProperties;
import com.glodon.zuul.utils.RateLimitExceededException;
import com.glodon.zuul.utils.RateLimitUtils;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.glodon.zuul.constant.RateLimitConstants.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * Created by liuqc-b on 2018/7/23.
 */
public class RateLimitPreFilter extends AbstractRateLimitFilter {

    private RouteLocator routeLocator;
    private UrlPathHelper urlPathHelper;

    private final RateLimitProperties properties;
    private final RateLimiter rateLimiter;
    private final RateLimitKeyGenerator rateLimitKeyGenerator;

    public RateLimitPreFilter(RateLimitProperties properties, RouteLocator routeLocator,
                              UrlPathHelper urlPathHelper, RateLimiter rateLimiter, RateLimitKeyGenerator rateLimitKeyGenerator,
                              RateLimitUtils rateLimitUtils) {
        super(properties, routeLocator, urlPathHelper, rateLimitUtils);
        this.properties = properties;
        this.rateLimiter = rateLimiter;
        this.rateLimitKeyGenerator = rateLimitKeyGenerator;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return properties.getPreFilterOrder();
    }

    @Override
    public Object run() {
        {
            final RequestContext ctx = RequestContext.getCurrentContext();
            final HttpServletResponse response = ctx.getResponse();
            final HttpServletRequest request = ctx.getRequest();
            final Route route = route(request);

            policy(route, request).forEach(policy -> {
                final String key = rateLimitKeyGenerator.key(request, route, policy);
                final Rate rate = rateLimiter.consume(policy, key, null);
                final String httpHeaderKey = key.replaceAll("[^A-Za-z0-9-.]", "_").replaceAll("__", "_");

                final Long limit = policy.getLimit();
                final Long remaining = rate.getRemaining();
                if (limit != null) {
                    response.setHeader(HEADER_LIMIT + httpHeaderKey, String.valueOf(limit));
                    response.setHeader(HEADER_REMAINING + httpHeaderKey, String.valueOf(Math.max(remaining, 0)));
                }

                final Long quota = policy.getQuota();
                final Long remainingQuota = rate.getRemainingQuota();
                if (quota != null) {
                    request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
                    response.setHeader(HEADER_QUOTA + httpHeaderKey, String.valueOf(quota));
                    response.setHeader(HEADER_REMAINING_QUOTA + httpHeaderKey,
                            String.valueOf(MILLISECONDS.toSeconds(Math.max(remainingQuota, 0))));
                }

                response.setHeader(HEADER_RESET + httpHeaderKey, String.valueOf(rate.getReset()));

                if ((limit != null && remaining < 0) || (quota != null && remainingQuota < 0)) {
                    ctx.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
                    ctx.put("rateLimitExceeded", "true");
                    ctx.setSendZuulResponse(false);
                    throw new RateLimitExceededException();
                }
            });
            return null;
        }
    }



}
