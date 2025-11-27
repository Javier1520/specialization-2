package com.epam.gym.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym.exception.ValidationException;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.util.LogUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthenticationInterceptorTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private LogUtils logUtils;

    @InjectMocks
    private AuthenticationInterceptor authenticationInterceptor;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void preHandle_publicEndpoint_returnsTrue() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/register");
        when(request.getMethod()).thenReturn("POST");

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(authenticationService, never()).authenticate(any(), any());
    }

    @Test
    void preHandle_publicLoginEndpoint_returnsTrue() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getMethod()).thenReturn("GET");

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(authenticationService, never()).authenticate(any(), any());
    }

    @Test
    void preHandle_missingUsername_returnsFalse() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("username")).thenReturn(null);
        when(request.getHeader("password")).thenReturn("password123");

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, null);

        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(authenticationService, never()).authenticate(any(), any());
    }

    @Test
    void preHandle_missingPassword_returnsFalse() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("username")).thenReturn("john.doe");
        when(request.getHeader("password")).thenReturn(null);

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, null);

        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(authenticationService, never()).authenticate(any(), any());
    }

    @Test
    void preHandle_blankUsername_returnsFalse() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("username")).thenReturn("   ");
        when(request.getHeader("password")).thenReturn("password123");

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, null);

        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(authenticationService, never()).authenticate(any(), any());
    }

    @Test
    void preHandle_validCredentials_returnsTrue() throws Exception {
        // Given
        String username = "john.doe";
        String password = "password123";

        when(request.getRequestURI()).thenReturn("/api/v1/trainees/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("username")).thenReturn(username);
        when(request.getHeader("password")).thenReturn(password);
        doNothing().when(authenticationService).authenticate(username, password);

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(authenticationService).authenticate(eq(username), eq(password));
        verify(request).setAttribute("authenticatedUsername", username);
    }

    @Test
    void preHandle_invalidCredentials_returnsFalse() throws Exception {
        // Given
        String username = "john.doe";
        String password = "wrongpassword";

        when(request.getRequestURI()).thenReturn("/api/v1/trainees/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("username")).thenReturn(username);
        when(request.getHeader("password")).thenReturn(password);
        doThrow(new ValidationException("Invalid credentials"))
                .when(authenticationService).authenticate(username, password);

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, null);

        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(authenticationService).authenticate(eq(username), eq(password));
    }

    @Test
    void preHandle_publicTrainerRegistration_returnsTrue() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/register");
        when(request.getMethod()).thenReturn("POST");

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(authenticationService, never()).authenticate(any(), any());
    }

    @Test
    void preHandle_privateEndpoint_requiresAuthentication() throws Exception {
        // Given
        String username = "john.doe";
        String password = "password123";

        when(request.getRequestURI()).thenReturn("/api/v1/trainings");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("username")).thenReturn(username);
        when(request.getHeader("password")).thenReturn(password);
        doNothing().when(authenticationService).authenticate(username, password);

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(authenticationService).authenticate(eq(username), eq(password));
    }
}

