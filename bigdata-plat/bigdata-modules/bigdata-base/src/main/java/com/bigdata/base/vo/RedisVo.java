package com.bigdata.base.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Bigdata
 */
@Data
@AllArgsConstructor
public class RedisVo {

    private String key;

    private String value;

    private Long expireTime;
}
