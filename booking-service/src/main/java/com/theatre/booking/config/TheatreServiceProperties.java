package com.theatre.booking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.theatre-service")
public record TheatreServiceProperties(
        String baseUrl,
        String internalToken
) {}
