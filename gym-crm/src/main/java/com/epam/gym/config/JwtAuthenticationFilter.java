package com.epam.gym.config;

import com.epam.gym.security.JwtService;
import com.epam.gym.util.LogUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int TOKEN_BEGIN_INDEX = BEARER_PREFIX.length();
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final LogUtils logUtils;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   LogUtils logUtils,
                                   ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.logUtils = logUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
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
            } catch (UsernameNotFoundException ex) {
                handleAuthenticationException(response, request.getRequestURI(),
                        "User not found: " + ex.getMessage());
                return;
            } catch (Exception ex) {
                logUtils.error(log, "Unexpected authentication error for URI {}: {}",
                        request.getRequestURI(), ex.getMessage());
                        handleAuthenticationException(response, request.getRequestURI(),
                        "Authentication failed");
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

        authenticateUser(request, username, token);
        logUtils.debug(log, "Successfully authenticated user: {}", username);
    }

    private void authenticateUser(HttpServletRequest request, String username, String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                userDetails.getAuthorities()
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void handleJwtException(HttpServletResponse response, String path, JwtException ex)
            throws IOException {
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

    private void handleAuthenticationException(HttpServletResponse response, String path, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder(
                        new RuntimeException(message),
                        HttpStatus.UNAUTHORIZED,
                        message)
                .title("Authentication Failed")
                .property("path", path)
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}