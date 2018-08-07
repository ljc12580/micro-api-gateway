package com.glodon.zuul.config;

import com.glodon.zuul.filter.RateLimitPostFilter;
import com.glodon.zuul.filter.RateLimitPreFilter;
import com.glodon.zuul.handler.DefaultRateLimiterErrorHandler;
import com.glodon.zuul.handler.RateLimiterErrorHandler;
import com.glodon.zuul.properties.RateLimitProperties;
import com.glodon.zuul.repository.RedisRateLimiter;
import com.glodon.zuul.utils.RateLimitUtils;
import com.glodon.zuul.utils.StringToMatchTypeConverter;
import com.google.common.collect.Lists;
import com.netflix.zuul.ZuulFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.util.UrlPathHelper;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.glodon.zuul.properties.RateLimitProperties.PREFIX;


/**
 * Created by liuqc-b on 2018/7/27.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class RateLimitAutoConfiguration {

    @Bean
    @ConfigurationPropertiesBinding
    public StringToMatchTypeConverter stringToMatchTypeConverter() {
        return new StringToMatchTypeConverter();
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiterErrorHandler.class)
    public RateLimiterErrorHandler rateLimiterErrorHandler() {
        return new DefaultRateLimiterErrorHandler();
    }

    @Bean
    public RateLimitUtils rateLimitUtils(RateLimitProperties rateLimitProperties) {
        return new RateLimitUtils(rateLimitProperties);
    }

    @Bean
    public ZuulFilter rateLimiterPreFilter(RateLimiter rateLimiter, RateLimitProperties rateLimitProperties,
                                           RouteLocator routeLocator, RateLimitKeyGenerator rateLimitKeyGenerator,
                                           RateLimitUtils rateLimitUtils) {
        return new RateLimitPreFilter(rateLimitProperties, routeLocator, new UrlPathHelper(), rateLimiter,
                rateLimitKeyGenerator, rateLimitUtils);
    }

    @Bean
    public ZuulFilter rateLimiterPostFilter(RateLimiter rateLimiter, RateLimitProperties rateLimitProperties,
                                            RouteLocator routeLocator, RateLimitKeyGenerator rateLimitKeyGenerator,
                                            RateLimitUtils rateLimitUtils) {
        return new RateLimitPostFilter(rateLimitProperties, routeLocator, new UrlPathHelper(), rateLimiter,
                rateLimitKeyGenerator, rateLimitUtils);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitKeyGenerator.class)
    public RateLimitKeyGenerator ratelimitKeyGenerator(RateLimitProperties properties, RateLimitUtils rateLimitUtils) {
        return new DefaultRateLimitKeyGenerator(properties, rateLimitUtils);
    }

    @Configuration
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "REDIS")
    public static class RedisConfiguration {

        @Bean("rateLimiterRedisTemplate")
        public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }

        @Bean
        public RateLimiter redisRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler,
                                            @Qualifier("rateLimiterRedisTemplate") RedisTemplate redisTemplate) {
            return new RedisRateLimiter(rateLimiterErrorHandler, redisTemplate);
        }
    }
//    @Configuration
//    @ConditionalOnMissingBean(RateLimiter.class)
//    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "IN_MEMORY", matchIfMissing = true)
//    public static class InMemoryConfiguration {
//
//        @Bean
//        public RateLimiter inMemoryRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler) {
//            return new InMemoryRateLimiter(rateLimiterErrorHandler);
//        }
//    }

    @Configuration
    @RequiredArgsConstructor
    protected static class RateLimitPropertiesAdjuster {

        private final RateLimitProperties rateLimitProperties;

        @PostConstruct
        public void init() {
            RateLimitProperties.Policy defaultPolicy = rateLimitProperties.getDefaultPolicy();
            if (defaultPolicy != null) {
                ArrayList<RateLimitProperties.Policy> defaultPolicies = Lists.newArrayList(defaultPolicy);
                defaultPolicies.addAll(rateLimitProperties.getDefaultPolicyList());
                rateLimitProperties.setDefaultPolicyList(defaultPolicies);
            }
            rateLimitProperties.getPolicies().forEach((route, policy) ->
                    rateLimitProperties.getPolicyList().compute(route, (key, policies) -> getPolicies(policy, policies)));
        }

        private List<RateLimitProperties.Policy> getPolicies(RateLimitProperties.Policy policy, List<RateLimitProperties.Policy> policies) {
            List<RateLimitProperties.Policy> combinedPolicies = Lists.newArrayList(policy);
            if (policies != null) {
                combinedPolicies.addAll(policies);
            }
            return combinedPolicies;
        }
    }
}


