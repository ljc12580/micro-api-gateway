package com.glodon.zuul.mapper;

import com.glodon.zuul.model.Policies;
import com.glodon.zuul.model.Rate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by liuqc-b on 2018/8/6.
 */
@Mapper
public interface PoliciesMapper {
    Policies queryPoliciesByTenant(@Param("key") String key, @Param("tenantId") String tenantId);
}
