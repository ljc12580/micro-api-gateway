package com.glodon.zuul.service;

import com.glodon.zuul.config.RateLimiter;
import com.glodon.zuul.handler.RateLimiterErrorHandler;
import com.glodon.zuul.model.Rate;
import com.glodon.zuul.properties.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by liuqc-b on 2018/7/27.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRateLimiter implements RateLimiter {

    private final RateLimiterErrorHandler rateLimiterErrorHandler;

    protected abstract Rate getRate(String key);
    protected abstract void saveRate(Rate rate);

    @Override
    public synchronized Rate consume(final RateLimitProperties.Policy policy, final String key, final Long requestTime) {
        Rate rate = this.create(policy, key);
        updateRate(policy, rate, requestTime);
        try {
            saveRate(rate);
        } catch (RuntimeException e) {
            rateLimiterErrorHandler.handleSaveError(key, e);
        }
        return rate;
    }

    private Rate create(final RateLimitProperties.Policy policy, final String key) {
        Rate rate = null;
        try {
            rate = this.getRate(key);
        } catch (RuntimeException e) {
            rateLimiterErrorHandler.handleFetchError(key, e);
        }

        if (!isExpired(rate)) {
            return rate;
        }

        Long limit = policy.getLimit();
        Long quota = policy.getQuota() != null ? SECONDS.toMillis(policy.getQuota()) : null;
        Long refreshInterval = SECONDS.toMillis(policy.getRefreshInterval());
        Date expiration = new Date(System.currentTimeMillis() + refreshInterval);

        return new Rate(key, limit, quota, refreshInterval, expiration);
    }

    private void updateRate(final RateLimitProperties.Policy policy, final Rate rate, final Long requestTime) {
        if (rate.getReset() > 0) {
            Long reset = rate.getExpiration().getTime() - System.currentTimeMillis();
            rate.setReset(reset);
        }
        if (policy.getLimit() != null && requestTime == null) {
            rate.setRemaining(Math.max(-1, rate.getRemaining() - 1));
        }
        if (policy.getQuota() != null && requestTime != null) {
            rate.setRemainingQuota(Math.max(-1, rate.getRemainingQuota() - requestTime));
        }
    }

    private boolean isExpired(final Rate rate) {
        return rate == null || (rate.getExpiration().getTime() < System.currentTimeMillis());
    }
}
