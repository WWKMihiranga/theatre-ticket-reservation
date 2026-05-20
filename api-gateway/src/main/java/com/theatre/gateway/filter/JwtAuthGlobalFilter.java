package com.theatre.gateway.filter;

import com.theatre.gateway.config.GatewaySecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Validates JWT on every request EXCEPT explicit public paths.
 *
 * Why validate here as well as in downstream services?
 *   1. Rejects unauthenticated traffic at the perimeter (smaller blast radius).
 *   2. Lets us inject trusted X-User-Id / X-User-Role headers downstream.
 *
 * Downstream services still validate themselves so they're not blindly trusting
 * any caller — defence in depth.
 */
@Component
@Slf4j
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    public static final String X_USER_ID = "X-User-Id";
    public static final String X_USER_ROLE = "X-User-Role";

    private final SecretKey signingKey;
    private final String expectedIssuer;
    private final List<String> publicPaths;

    public JwtAuthGlobalFilter(GatewaySecurityProperties props) {
        this.signingKey = Keys.hmacShaKeyFor(props.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.expectedIssuer = props.jwt().issuer();
        this.publicPaths = props.publicPaths() == null ? List.of() : props.publicPaths();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // Defence in depth: explicitly let CORS preflights through. The CorsWebFilter
        // also handles these earlier in the chain, but skipping here is safer in case
        // ordering ever changes.
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return chain.filter(exchange);
        }

        // Public paths skip auth
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        // GET /api/v1/shows/** is public (browsing shows doesn't require login)
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/v1/shows")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(expectedIssuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }

        // Inject identity headers for downstream services
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(X_USER_ID, claims.getSubject())
                .header(X_USER_ROLE, claims.get("role", String.class))
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isPublic(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer error=\"invalid_token\"");
        log.debug("Rejected request to {}: {}", exchange.getRequest().getURI(), reason);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // Run before routing
        return -100;
    }
}
