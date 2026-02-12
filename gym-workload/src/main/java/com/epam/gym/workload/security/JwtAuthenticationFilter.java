package com.epam.gym.workload.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int TOKEN_BEGIN_INDEX = BEARER_PREFIX.length();

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (hasBearerToken(request) && isNotAlreadyAuthenticated()) {
            try {
                String token = extractToken(request);
                processAuthentication(request, token);
            } catch (JwtException ex) {
                handleJwtException(response, request.getRequestURI(), ex);
                return;
            } catch (Exception ex) {
                handleAuthenticationException(
                        response, request.getRequestURI(), "Authentication failed");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    private boolean isNotAlreadyAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.length() > TOKEN_BEGIN_INDEX) {
            return authHeader.substring(TOKEN_BEGIN_INDEX);
        }
        throw new JwtException("Invalid Authorization header format");
    }

    private void processAuthentication(HttpServletRequest request, String token) {
        String username = jwtService.extractUsername(token);

        if (username == null || username.trim().isEmpty()) {
            throw new JwtException("Username cannot be extracted from token");
        }

        if (Boolean.FALSE.equals(jwtService.validateToken(token, username))) {
            throw new JwtException("Token validation failed");
        }

        authenticateUser(request, username);
    }

    private void authenticateUser(HttpServletRequest request, String username) {
        // Since gym-workload might not have a full UserDetailsService,
        // we can trust the token and create an authenticated token with default/extracted
        // authorities.
        // For simplicity, we assign a default role or extract roles from claims if available.
        // Assuming implicit trust for valid tokens from gym-crm.

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void handleJwtException(HttpServletResponse response, String path, JwtException ex)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse =
                ErrorResponse.builder(
                                ex,
                                HttpStatus.UNAUTHORIZED,
                                "JWT token is invalid or expired: " + ex.getMessage())
                        .title("Unauthorized")
                        .property("path", path)
                        .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private void handleAuthenticationException(
            HttpServletResponse response, String path, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse =
                ErrorResponse.builder(
                                new RuntimeException(message), HttpStatus.UNAUTHORIZED, message)
                        .title("Authentication Failed")
                        .property("path", path)
                        .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
