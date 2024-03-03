package com.pathfinder.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloudinary")
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class CloudinaryConfig {

    private String cloudName;

    private String apiKey;

    private String apiSecret;

    private Boolean secure;

}
