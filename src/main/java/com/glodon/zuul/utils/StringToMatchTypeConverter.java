package com.glodon.zuul.utils;

import com.glodon.zuul.properties.RateLimitProperties.Policy.MatchType;
import com.glodon.zuul.properties.RateLimitProperties.Policy.Type;
import org.springframework.core.convert.converter.Converter;

/**
 * Created by liuqc-b on 2018/7/27.
 */
public class StringToMatchTypeConverter implements Converter<String, MatchType> {

    private static final String DELIMITER = "=";

    @Override
    public MatchType convert(String type) {
        if (type.contains(DELIMITER)) {
            String[] matchType = type.split(DELIMITER);
            return new MatchType(Type.valueOf(matchType[0].toUpperCase()), matchType[1]);
        }
        return new MatchType(Type.valueOf(type.toUpperCase()), null);
    }
}
