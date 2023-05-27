package com.bigdata.activity.config;

import com.bigdata.activity.properties.ActivitiExtendProperties;
import lombok.extern.slf4j.Slf4j;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Bigdata
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ActivitiExtendProperties.class)
public class ActivitiConfig {

    @Autowired
    private ActivitiExtendProperties properties;

    @Bean
    public ProcessEngineConfigurationConfigurer processEngineConfigurationConfigurer() {

        ProcessEngineConfigurationConfigurer configurer = processEngineConfiguration -> {

            processEngineConfiguration.setActivityFontName(properties.getActivityFontName());
            processEngineConfiguration.setAnnotationFontName(properties.getActivityFontName());
            processEngineConfiguration.setLabelFontName(properties.getLabelFontName());
        };

        return configurer;
    }
}
