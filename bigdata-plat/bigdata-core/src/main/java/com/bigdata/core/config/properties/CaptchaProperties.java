package com.bigdata.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bigdata
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "captcha")
public class CaptchaProperties {

    private List<String> image = new ArrayList<>();

    private List<String> sms = new ArrayList<>();

    private List<String> email = new ArrayList<>();
}
