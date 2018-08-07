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
 * Created by liuqc-b on 2018/7/27.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Rate {

    @Id
    @Column(name = "rate_key")
    private String key;
    private Long remaining;
    private Long remainingQuota;
    private Long reset;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private Date expiration;

}
