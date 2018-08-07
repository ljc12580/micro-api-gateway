package com.glodon.zuul.service;

import com.glodon.zuul.config.RateLimiter;
import com.glodon.zuul.model.Rate;
import com.glodon.zuul.properties.RateLimitProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by liuqc-b on 2018/7/30.
 */
public abstract class AbstractCacheRateLimiter implements RateLimiter {

    @Autowired
    RateLimitProperties rateLimitProperties;


    @Override
    public synchronized Rate consume(RateLimitProperties.Policy policy, String key, Long requestTime) {
        final Long refreshInterval = policy.getRefreshInterval();
        final Long quota = policy.getQuota() != null ? SECONDS.toMillis(policy.getQuota()) : null;
        final Rate rate = new Rate(key, policy.getLimit(), quota, null, null);

        calcRemainingLimit(policy.getLimit(), refreshInterval, requestTime, key, rate);
        calcRemainingQuota(quota, refreshInterval, requestTime, key, rate);

        return rate;
    }

    protected abstract void calcRemainingLimit(Long limit, Long refreshInterval, Long requestTime, String key, Rate rate);

    protected abstract void calcRemainingQuota(Long quota, Long refreshInterval, Long requestTime, String key, Rate rate);
}
