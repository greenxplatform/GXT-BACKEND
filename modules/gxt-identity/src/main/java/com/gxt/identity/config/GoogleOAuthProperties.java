package com.gxt.identity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "gxt.oauth.google")
public class GoogleOAuthProperties {
    private String clientId = "";
}
