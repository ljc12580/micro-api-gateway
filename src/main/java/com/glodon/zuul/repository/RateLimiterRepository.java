package com.glodon.zuul.repository;

import com.glodon.zuul.model.Rate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;


/**
 * Created by liuqc-b on 2018/7/30.
 */
@Component
public interface RateLimiterRepository extends CrudRepository<Rate, String>{
}
