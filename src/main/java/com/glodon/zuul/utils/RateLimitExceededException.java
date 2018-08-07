package com.glodon.zuul.utils;

import com.netflix.zuul.exception.ZuulException;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;

/**
 * Created by liuqc-b on 2018/7/27.
 */
public class RateLimitExceededException extends ZuulRuntimeException {

    public RateLimitExceededException() {
        super(new ZuulException(HttpStatus.TOO_MANY_REQUESTS.toString(), HttpStatus.TOO_MANY_REQUESTS.value(), null));
    }
}
