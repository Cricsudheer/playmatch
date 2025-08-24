package com.example.playmatch.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI().info(new Info().title("App API").version("v1"));
    }
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder().group("public").pathsToMatch("/**").build();
    }
}
