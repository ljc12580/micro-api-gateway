//package com.glodon.zuul.service;
//
//import com.glodon.zuul.handler.RateLimiterErrorHandler;
//import com.glodon.zuul.model.Rate;
//import com.glodon.zuul.properties.RateLimitProperties;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Created by liuqc-b on 2018/7/27.
// */
//public class InMemoryRateLimiter extends AbstractRateLimiter {
//
//    private Map<String, Rate> repository = new ConcurrentHashMap<>();
//
//    public InMemoryRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler) {
//        super(rateLimiterErrorHandler);
//    }
//
//    @Override
//    protected Rate getRate(String key) {
//        return this.repository.get(key);
//    }
//
//    @Override
//    protected void saveRate(Rate rate) {
//        this.repository.put(rate.getKey(), rate);
//    }
//
//}