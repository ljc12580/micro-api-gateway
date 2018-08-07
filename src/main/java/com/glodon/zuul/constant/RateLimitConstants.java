package com.glodon.zuul.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Created by liuqc-b on 2018/7/27.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RateLimitConstants {

    public static final String HEADER_QUOTA = "X-RateLimit-Quota-";
    public static final String HEADER_REMAINING_QUOTA = "X-RateLimit-Remaining-Quota-";
    public static final String HEADER_LIMIT = "X-RateLimit-Limit-";
    public static final String HEADER_REMAINING = "X-RateLimit-Remaining-";
    public static final String HEADER_RESET = "X-RateLimit-Reset-";
    public static final String REQUEST_START_TIME = "rateLimitRequestStartTime";

}