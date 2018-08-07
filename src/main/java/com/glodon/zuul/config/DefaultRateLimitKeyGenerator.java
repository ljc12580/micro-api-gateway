package com.glodon.zuul.config;

import com.glodon.zuul.properties.RateLimitProperties.Policy.MatchType;

import java.util.Map;
import java.util.StringJoiner;
import javax.servlet.http.HttpServletRequest;

import com.glodon.zuul.properties.RateLimitProperties;
import com.glodon.zuul.utils.RateLimitUtils;
import com.glodon.zuul.properties.RateLimitProperties.Policy;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;
/**
 * Created by liuqc-b on 2018/7/24.
 */

@RequiredArgsConstructor
public class DefaultRateLimitKeyGenerator implements RateLimitKeyGenerator {

    private final RateLimitProperties properties;
    private final RateLimitUtils rateLimitUtils;

    @Override
    public String key(final HttpServletRequest request, final Route route, final Policy policy) {
        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(properties.getKeyPrefix());
        if (route != null) {
            joiner.add(route.getId());
        }
        policy.getType().forEach(matchType -> {
            if (route != null && Policy.Type.URL.equals(matchType.getType())) {
                joiner.add(route.getPath());
                addMatcher(joiner, matchType);
            }
            if (Policy.Type.ORIGIN.equals(matchType.getType())) {
                joiner.add(rateLimitUtils.getRemoteAddress(request));
                addMatcher(joiner, matchType);
            }
            if (Policy.Type.USER.equals(matchType.getType())) {
                joiner.add(rateLimitUtils.getUser(request));
                addMatcher(joiner, matchType);
            }
            if (Policy.Type.TENANT.equals(matchType.getType())) {
//                RequestContext requestContext=RequestContext.getCurrentContext();
//                Map headerMap = requestContext.getZuulRequestHeaders();
//                joiner.add(headerMap.get("x-tenant-id").toString());
                joiner.add(rateLimitUtils.getTenant(request));
                addMatcher(joiner, matchType);
            }
        });
        return joiner.toString();
    }

    private void addMatcher(StringJoiner joiner, MatchType matchType) {
        if (StringUtils.isNotEmpty(matchType.getMatcher())) {
            joiner.add(matchType.getMatcher());
        }
    }
}
