package com.theatre.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class GatewayConfig {

    /**
     * Explicit CorsWebFilter for routed requests.
     *
     * Spring Cloud Gateway's `spring.cloud.gateway.globalcors` property handles
     * CORS for the gateway's own endpoints (e.g. /actuator), but NOT reliably for
     * requests forwarded to downstream services. For those, we register a
     * CorsWebFilter that runs in the reactive filter chain and adds CORS headers
     * to every response — including preflight OPTIONS responses.
     *
     * This is the single source of CORS truth for the system. Downstream services
     * deliberately do not configure CORS — they're never hit by browsers.
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://theatre-system.netlify.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
