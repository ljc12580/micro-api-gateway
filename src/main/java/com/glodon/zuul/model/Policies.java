package com.glodon.zuul.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by liuqc-b on 2018/8/6.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Policies {
    private Integer id;
    private String serviceId;
    private String tenantId;
    private Long limit;
    private Long quota;
    private Long refreshInterval;
}
