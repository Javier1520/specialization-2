package com.epam.gym.config;

import com.epam.gym.security.JwtService;
import com.epam.gym.util.LogUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEGIN_TOKEN = BEARER_PREFIX.length();
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final LogUtils logUtils;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, LogUtils logUtils,
                                   ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.logUtils = logUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                                        throws ServletException, IOException {

        if (hasBearerToken(request) && isNotAlreadyAuthenticated()) {
            try {
                processAuthentication(request, extractToken(request));
            } catch (JwtException ex) {
                handleJwtException(response, request.getRequestURI(), ex);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasBearerToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        return authHeader != null && isBearer(authHeader);
    }

    private boolean isBearer(String authHeader) {
        return authHeader.startsWith(BEARER_PREFIX);
    }

    private boolean isNotAlreadyAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || !authentication.isAuthenticated();
    }

    private void processAuthentication(HttpServletRequest request, String token) {
        final String username = Objects.requireNonNull(jwtService.extractUsername(token),
                "Username extracted from JWT must not be null");
        authenticateUser(request, username);
    }

    private void authenticateUser(HttpServletRequest request, String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private String extractToken(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_HEADER).substring(BEGIN_TOKEN);
    }

    private void handleJwtException(HttpServletResponse response, String path, JwtException ex)
            throws IOException{
        logUtils.warn(log, "JWT validation failed for URI {}: {}", path, ex.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED,
                        "JWT token is invalid or expired: " + ex.getMessage())
                .title("Unauthorized")
                .property("path", path)
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
