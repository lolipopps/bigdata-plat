package com.bigdata.activity.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Bigdata
 */
@Data
@ConfigurationProperties(prefix = "spring.activity.bigdata")
public class ActivitiExtendProperties {

    /**
     * 流程图字体配置
     */
    private String activityFontName = "宋体";

    /**
     * 流程图字体配置
     */
    private String labelFontName = "宋体";
}
