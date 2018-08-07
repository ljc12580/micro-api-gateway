package com.glodon.zuul.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by liuqc-b on 2018/7/27.
 */
@Slf4j
public class DefaultRateLimiterErrorHandler implements RateLimiterErrorHandler {

    @Override
    public void handleSaveError(String key, Exception e) {
        log.error("Failed saving rate for " + key + ", returning unsaved rate", e);
    }

    @Override
    public void handleFetchError(String key, Exception e) {
        log.error("Failed retrieving rate for " + key + ", will create new rate", e);
    }

    @Override
    public void handleError(String msg, Exception e) {
        log.error(msg, e);
    }
}
