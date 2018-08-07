package com.glodon.zuul.config;

import com.glodon.zuul.model.Rate;
import com.glodon.zuul.properties.RateLimitProperties;
import com.glodon.zuul.properties.RateLimitProperties.Policy;

/**
 * Created by liuqc-b on 2018/7/27.
 */
public interface RateLimiter {

    String QUOTA_SUFFIX = "-quota";

    /**
     * @param policy      Template for which rates should be created in case there's no rate limit associated with the
     *                    key
     * @param key         Unique key that identifies a request
     * @param requestTime The total time it took to handle the request
     * @return a view of a user's rate request limit
     */
    Rate consume(Policy policy, String key, Long requestTime);
}

