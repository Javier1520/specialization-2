package com.epam.gym.config;

import com.epam.gym.security.JwtService;
import com.epam.gym.util.LogUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private LogUtils logUtils;
    @Mock private ObjectMapper objectMapper;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        // Set up writer for response
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_noAuthorizationHeader_shouldContinueFilter()
            throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_authHeaderWithoutBearer_shouldContinueFilter()
            throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_validToken_shouldSetAuthentication()
            throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String username = "testuser";
        UserDetails userDetails =
                User.builder()
                        .username(username)
                        .password("password123456")
                        .authorities(Collections.emptyList())
                        .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.validateToken(token, username)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(
                username,
                ((UserDetails)
                        SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getPrincipal())
                        .getUsername());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_shouldReturnUnauthorizedAndNotContinueFilter()
            throws ServletException, IOException {
        // Given
        String token = "invalid.jwt.token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.validateToken(token, username)).thenReturn(false);
        when(response.getWriter()).thenReturn(writer);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_jwtExceptionDuringTokenProcessing_shouldReturnUnauthorizedAndNotContinueFilter()
            throws ServletException, IOException {
        // Given
        String token = "malformed.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtService.extractUsername(token)).thenThrow(new JwtException("Invalid token format"));
        when(response.getWriter()).thenReturn(writer);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(logUtils).warn(any(), eq("JWT validation failed for URI {}: {}"),
                eq("/api/test"), anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_usernameNotFoundException_shouldReturnUnauthorizedAndNotContinueFilter()
            throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String username = "nonexistent";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.validateToken(token, username)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(username))
                .thenThrow(new UsernameNotFoundException("User not found"));
        when(response.getWriter()).thenReturn(writer);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_nullUsername_shouldReturnUnauthorizedAndNotContinueFilter()
            throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtService.extractUsername(token)).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_emptyUsername_shouldReturnUnauthorizedAndNotContinueFilter()
            throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtService.extractUsername(token)).thenReturn("   "); // Empty/whitespace
        when(response.getWriter()).thenReturn(writer);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_existingAuthentication_shouldSkipAuthenticationAndContinueFilter()
            throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";

        // Set existing authentication with proper password length
        UserDetails existingUser =
                User.builder()
                        .username("existinguser")
                        .password("securePassword123")
                        .authorities(Collections.emptyList())
                        .build();

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                existingUser, null, existingUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // Should keep existing authentication unchanged
        assertEquals(
                "existinguser",
                ((UserDetails)
                        SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getPrincipal())
                        .getUsername());
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_emptyBearerToken_shouldReturnUnauthorizedAndNotContinueFilter()
            throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getWriter()).thenReturn(writer);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_unexpectedException_shouldReturnUnauthorizedAndNotContinueFilter()
            throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtService.extractUsername(token))
                .thenThrow(new RuntimeException("Unexpected error"));
        when(response.getWriter()).thenReturn(writer);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(logUtils).error(any(), eq("Unexpected authentication error for URI {}: {}"),
                eq("/api/test"), anyString());
        verify(filterChain, never()).doFilter(request, response);
    }
}