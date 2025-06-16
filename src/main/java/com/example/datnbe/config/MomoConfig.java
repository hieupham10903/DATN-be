package com.example.datnbe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix="momo")
public class MomoConfig {
    private String baseUrl;
    private String targetEnvironment;
    private CollectionConfig collection;

    @Getter @Setter
    public static class CollectionConfig {
        private String primaryKey, userId, apiSecret;
    }
}
