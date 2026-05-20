package com.theatre.theatre.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates internal service-to-service calls via a shared secret header.
 * The Booking Service sends X-Internal-Token on every request to /seats/book and /seats/release.
 *
 * This is a deliberately simple mechanism — it works because we control both ends
 * and only short-circuits to grant ROLE_SERVICE when the secret matches.
 * Real production systems would use mTLS or signed service tokens.
 */
@Component
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    public static final String INTERNAL_HEADER = "X-Internal-Token";

    private final String expectedToken;

    public InternalServiceAuthFilter(@Value("${app.security.internal-token}") String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader(INTERNAL_HEADER);
        if (token != null && token.equals(expectedToken)) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "internal-service",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
