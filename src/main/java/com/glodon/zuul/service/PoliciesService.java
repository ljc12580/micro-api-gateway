package com.glodon.zuul.service;

import com.glodon.zuul.mapper.PoliciesMapper;
import com.glodon.zuul.model.Policies;
import com.glodon.zuul.model.Rate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by liuqc-b on 2018/8/6.
 */
@Service
public class PoliciesService implements PoliciesMapper{
    @Autowired
    PoliciesMapper policiesMapper;

    @Override
    public Policies queryPoliciesByTenant(String key, String tenantId) {
        return policiesMapper.queryPoliciesByTenant(key,tenantId);
    }
}
