package com.glodon.zuul.filter;

import com.glodon.zuul.properties.RateLimitProperties;
import com.glodon.zuul.properties.RateLimitProperties.Policy.MatchType;
import com.glodon.zuul.utils.RateLimitUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by liuqc-b on 2018/7/27.
 */
@RequiredArgsConstructor
public abstract class AbstractRateLimitFilter extends ZuulFilter {

    private final RateLimitProperties properties;
    private final RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper;
    private final RateLimitUtils rateLimitUtils;

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        return properties.isEnabled() && !policy(route(request), request).isEmpty();
    }

    Route route(HttpServletRequest request) {
        String requestURI = urlPathHelper.getPathWithinApplication(request);
        return routeLocator.getMatchingRoute(requestURI);
    }

    protected List<RateLimitProperties.Policy> policy(Route route, HttpServletRequest request) {
        List<RateLimitProperties.Policy> applicablePolicies = properties.getDefaultPolicyList().stream()
                .filter(policy -> applyPolicy(request, route, policy))
                .collect(Collectors.toList());
        if (route != null) {
            properties.getPolicies(route.getId()).stream()
                    .filter(policy -> applyPolicy(request, route, policy))
                    .forEach(applicablePolicies::add);
        }
        return applicablePolicies;
    }

    private boolean applyPolicy(HttpServletRequest request, Route route, RateLimitProperties.Policy policy) {
        List<MatchType> types = policy.getType();
        return types.isEmpty() || (urlApply(types, route) && originApply(types, request) && userApply(types, request));
    }

    private boolean userApply(List<MatchType> types, HttpServletRequest request) {
        List<String> users = getConfiguredType(types, RateLimitProperties.Policy.Type.USER);

        return users.isEmpty()
                || users.contains(rateLimitUtils.getUser(request));
    }

    private boolean originApply(List<MatchType> types, HttpServletRequest request) {
        List<String> origins = getConfiguredType(types, RateLimitProperties.Policy.Type.ORIGIN);

        return origins.isEmpty()
                || origins.contains(rateLimitUtils.getRemoteAddress(request));
    }

    private boolean urlApply(List<MatchType> types, Route route) {
        List<String> urls = getConfiguredType(types, RateLimitProperties.Policy.Type.URL);

        return urls.isEmpty()
                || route == null
                || urls.stream().anyMatch(url -> route.getPath().startsWith(url));
    }

    private List<String> getConfiguredType(List<MatchType> types, RateLimitProperties.Policy.Type user) {
        return types.stream()
                .filter(matchType -> matchType.getType().equals(user))
                .map(MatchType::getMatcher)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

