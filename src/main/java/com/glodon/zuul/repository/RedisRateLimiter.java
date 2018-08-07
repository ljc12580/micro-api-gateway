package com.glodon.zuul.repository;

import com.glodon.zuul.handler.RateLimiterErrorHandler;
import com.glodon.zuul.model.Policies;
import com.glodon.zuul.model.Rate;
import com.glodon.zuul.service.AbstractCacheRateLimiter;
import com.glodon.zuul.service.PoliciesService;
import com.glodon.zuul.utils.UrlUtils;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by liuqc-b on 2018/7/30.
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class RedisRateLimiter extends AbstractCacheRateLimiter {

    @Autowired
    PoliciesService policiesService;
    @Autowired
    UrlUtils urlUtils;
    private final RateLimiterErrorHandler rateLimiterErrorHandler;
    private final RedisTemplate redisTemplate;
    private Map<String, Long> repository = new ConcurrentHashMap<>();

    @Override
    protected void calcRemainingLimit(Long limit, Long refreshInterval,
                                      Long requestTime, String key, Rate rate) {
        Policies policies = new Policies();
        if (limit != null) {
            handleExpiration(key, refreshInterval, rate);
            long usage = requestTime == null ? 1L : 0L;
            Long current = 0L;
            Object limitExist=redisTemplate.boundValueOps(key).get();
            if(limitExist==null&&repository.get("limit")==null){
                RequestContext requestContext=RequestContext.getCurrentContext();
                HttpServletRequest httpServletRequest=requestContext.getRequest();
                final Route route = urlUtils.route(httpServletRequest);
                String tenantId=httpServletRequest.getHeader("x-tenant-id");
                String key1=route.getId();
                policies=policiesService.queryPoliciesByTenant(key1,tenantId);
                repository.put("limit",policies.getLimit());
            }
            if(repository.get("limit")!=null){
                limit=repository.get("limit");
            }
            try {
                current = this.redisTemplate.boundValueOps(key).increment(usage);
            } catch (RuntimeException e) {
                String msg = "Failed retrieving rate for " + key + ", will return limit";
                rateLimiterErrorHandler.handleError(msg, e);
            }
            rate.setRemaining(Math.max(-1, limit - current));
        }
    }

    @Override
    protected void calcRemainingQuota(Long quota, Long refreshInterval,
                                      Long requestTime, String key, Rate rate) {
        if (quota != null) {
            String quotaKey = key + QUOTA_SUFFIX;
            handleExpiration(quotaKey, refreshInterval, rate);
            Long usage = requestTime != null ? requestTime : 0L;
            Long current = 0L;
            try {
                current = this.redisTemplate.boundValueOps(quotaKey).increment(usage);
            } catch (RuntimeException e) {
                String msg = "Failed retrieving rate for " + quotaKey + ", will return quota limit";
                rateLimiterErrorHandler.handleError(msg, e);
            }
            rate.setRemainingQuota(Math.max(-1, quota - current));
        }
    }

    private void handleExpiration(String key, Long refreshInterval, Rate rate) {
        Long expire = null;
        try {
            expire = this.redisTemplate.getExpire(key);
            if (expire == null || expire == -1) {
                this.redisTemplate.expire(key, refreshInterval, SECONDS);
                expire = refreshInterval;
            }
        } catch (RuntimeException e) {
            String msg = "Failed retrieving expiration for " + key + ", will reset now";
            rateLimiterErrorHandler.handleError(msg, e);
        }
        rate.setReset(SECONDS.toMillis(expire == null ? 0L : expire));
    }
}
