package com.rafaiska.telelogger.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebCorsConfiguration implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebCorsConfiguration(
            @Value("${TELELOGGER_CORS_ALLOWED_ORIGINS:https://html-classic.itch.zone}") String origins) {
        allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        for (String path : new String[] {
                "/api/sessions", "/api/sessions/", "/api/commands", "/api/commands/"}) {
            registry.addMapping(path)
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods("POST")
                    .allowedHeaders("Content-Type")
                    .allowCredentials(false)
                    .maxAge(3600);
        }
    }
}
