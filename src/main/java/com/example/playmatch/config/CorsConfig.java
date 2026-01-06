package com.example.playmatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.security.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${app.security.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.security.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.security.cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${app.security.cors.max-age}")
    private long maxAge;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        config.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
        config.setMaxAge(maxAge);
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
