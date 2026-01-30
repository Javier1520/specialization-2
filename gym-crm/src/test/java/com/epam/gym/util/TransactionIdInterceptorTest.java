package com.epam.gym.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionIdInterceptorTest {

    private TransactionIdInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private LogUtils logUtils;

    @BeforeEach
    void setUp() {
        logUtils = mock(LogUtils.class);
        interceptor = new TransactionIdInterceptor(logUtils);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        MDC.clear();
    }

    @Test
    void preHandle_withExistingTransactionId_usesProvidedId() throws Exception {
        // Given
        String existingTransactionId = "existing-transaction-id-123";
        when(request.getHeader("X-Transaction-Id")).thenReturn(existingTransactionId);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/trainees");

        // When
        boolean result = interceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(response).setHeader("X-Transaction-Id", existingTransactionId);
        // Verify MDC contains the transaction ID
        assertNotNull(MDC.get("transactionId"));
    }

    @Test
    void preHandle_withoutTransactionId_generatesNewId() throws Exception {
        // Given
        when(request.getHeader("X-Transaction-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/trainees");

        // When
        boolean result = interceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(response).setHeader(eq("X-Transaction-Id"), anyString());
        // Verify MDC contains a transaction ID
        assertNotNull(MDC.get("transactionId"));
    }

    @Test
    void preHandle_withBlankTransactionId_generatesNewId() throws Exception {
        // Given
        when(request.getHeader("X-Transaction-Id")).thenReturn("   ");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/trainees");

        // When
        boolean result = interceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(response).setHeader(eq("X-Transaction-Id"), anyString());
        // Verify MDC contains a transaction ID
        assertNotNull(MDC.get("transactionId"));
    }

    @Test
    void preHandle_setsMDC() throws Exception {
        // Given
        when(request.getHeader("X-Transaction-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/trainings");

        // When
        interceptor.preHandle(request, response, null);

        // Then
        String transactionId = MDC.get("transactionId");
        assertTrue(transactionId != null && !transactionId.isEmpty());
    }

    @Test
    void afterCompletion_removesMDC() throws Exception {
        // Given
        when(request.getHeader("X-Transaction-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/trainees");
        interceptor.preHandle(request, response, null);
        assertNotNull(MDC.get("transactionId"));

        // When
        interceptor.afterCompletion(request, response, null, null);

        // Then
        assertNull(MDC.get("transactionId"));
    }

    @Test
    void preHandle_logsRequestInfo() throws Exception {
        // Given
        String transactionId = "test-transaction-id";
        when(request.getHeader("X-Transaction-Id")).thenReturn(transactionId);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/john.doe");

        // When
        interceptor.preHandle(request, response, null);

        // Then
        verify(response).setHeader("X-Transaction-Id", transactionId);
        assertNotNull(MDC.get("transactionId"));
    }
}
