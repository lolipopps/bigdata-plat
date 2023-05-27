package com.bigdata.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Bigdata
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigdata.ratelimit")
public class BigdataLimitProperties {

    /**
     * 是否开启全局限流
     */
    private Boolean enable = false;

    /**
     * 限制请求个数
     */
    private Long limit = 100L;

    /**
     * 每单位时间内（毫秒）
     */
    private Long timeout = 1000L;
}
