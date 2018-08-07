package com.glodon.zuul.handler;

/**
 * Created by liuqc-b on 2018/7/27.
 */
public interface RateLimiterErrorHandler {

    void handleSaveError(String key, Exception e);

    void handleFetchError(String key, Exception e);

    void handleError(String msg, Exception e);

}
