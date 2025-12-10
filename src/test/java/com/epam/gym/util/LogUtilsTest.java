package com.epam.gym.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class LogUtilsTest {

    @Mock private Logger logger;

    @Captor private ArgumentCaptor<String> messageCaptor;

    @Captor private ArgumentCaptor<Object[]> argsCaptor;

    private LogUtils logUtils;

    @BeforeEach
    void setUp() {
        logUtils = new LogUtils();
    }

    @Test
    void info_withMessageOnly_shouldCallLoggerInfo() {
        // Given
        String message = "Test info message";

        // When
        logUtils.info(logger, message);

        // Then
        verify(logger).info(eq(message), any(Object[].class));
    }

    @Test
    void info_withMessageAndArgs_shouldCallLoggerInfoWithArgs() {
        // Given
        String message = "User {} logged in from {}";
        String username = "testuser";
        String ip = "192.168.1.1";

        // When
        logUtils.info(logger, message, username, ip);

        // Then
        verify(logger).info(eq(message), any(Object[].class));
    }

    @Test
    void warn_withMessageOnly_shouldCallLoggerWarn() {
        // Given
        String message = "Test warning message";

        // When
        logUtils.warn(logger, message);

        // Then
        verify(logger).warn(eq(message), any(Object[].class));
    }

    @Test
    void warn_withMessageAndArgs_shouldCallLoggerWarnWithArgs() {
        // Given
        String message = "Failed login attempt for user {}";
        String username = "testuser";

        // When
        logUtils.warn(logger, message, username);

        // Then
        verify(logger).warn(eq(message), any(Object[].class));
    }

    @Test
    void error_withMessageOnly_shouldCallLoggerError() {
        // Given
        String message = "Test error message";

        // When
        logUtils.error(logger, message);

        // Then
        verify(logger).error(eq(message), any(Object[].class));
    }

    @Test
    void error_withMessageAndArgs_shouldCallLoggerErrorWithArgs() {
        // Given
        String message = "Error processing request for user {}";
        String username = "testuser";

        // When
        logUtils.error(logger, message, username);

        // Then
        verify(logger).error(eq(message), any(Object[].class));
    }

    @Test
    void error_withException_shouldCallLoggerErrorWithException() {
        // Given
        String message = "Exception occurred: {}";
        Exception exception = new RuntimeException("Test exception");

        // When
        logUtils.error(logger, message, exception.getMessage());

        // Then
        verify(logger).error(eq(message), any(Object[].class));
    }

    @Test
    void debug_withMessageOnly_shouldCallLoggerDebug() {
        // Given
        String message = "Test debug message";

        // When
        logUtils.debug(logger, message);

        // Then
        verify(logger).debug(eq(message), any(Object[].class));
    }

    @Test
    void debug_withMessageAndArgs_shouldCallLoggerDebugWithArgs() {
        // Given
        String message = "Processing request {} for user {}";
        String requestId = "REQ-123";
        String username = "testuser";

        // When
        logUtils.debug(logger, message, requestId, username);

        // Then
        verify(logger).debug(eq(message), any(Object[].class));
    }

    @Test
    void info_withMultipleArgs_shouldPassAllArgs() {
        // Given
        String message = "Event: {}, User: {}, Action: {}, Status: {}";
        Object[] args = {"LOGIN", "testuser", "AUTHENTICATE", "SUCCESS"};

        // When
        logUtils.info(logger, message, args);

        // Then
        verify(logger).info(eq(message), any(Object[].class));
    }

    @Test
    void warn_withNullArgs_shouldHandleGracefully() {
        // Given
        String message = "Test message";

        // When
        logUtils.warn(logger, message, (Object[]) null);

        // Then
        verify(logger).warn(eq(message), (Object[]) isNull());
    }

    @Test
    void error_withEmptyArgs_shouldHandleGracefully() {
        // Given
        String message = "Test message";

        // When
        logUtils.error(logger, message);

        // Then
        verify(logger).error(eq(message), any(Object[].class));
    }

    @Test
    void debug_withComplexObject_shouldPassObject() {
        // Given
        String message = "User details: {}";
        Object userObject =
                new Object() {
                    @Override
                    public String toString() {
                        return "User[id=1, name=test]";
                    }
                };

        // When
        logUtils.debug(logger, message, userObject);

        // Then
        verify(logger).debug(eq(message), any(Object[].class));
    }
}
