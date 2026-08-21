package com.salarymanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class CorsConfig {

    /**
     * Origins that must always be allowed: local development and the deployed Netlify
     * frontend. Extra origins can be added via CORS_ALLOWED_ORIGINS (comma-separated),
     * but they can only extend this list, never remove these baseline origins.
     */
    private static final List<String> REQUIRED_ORIGINS = List.of(
            "http://localhost:4200",
            "https://jocular-druid-283316.netlify.app"
    );

    @Value("${app.cors.allowed-origins:}")
    private String extraOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        Set<String> origins = new LinkedHashSet<>(REQUIRED_ORIGINS);
        Arrays.stream(extraOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(origins::add);

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.copyOf(origins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
