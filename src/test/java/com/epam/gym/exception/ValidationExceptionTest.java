package com.epam.gym.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ValidationExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        // Given
        String message = "Validation failed";

        // When
        ValidationException exception = new ValidationException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_withNullMessage_handlesGracefully() {
        // When
        ValidationException exception = new ValidationException(null);

        // Then
        assertNull(exception.getMessage());
    }

    @Test
    void exception_canBeThrown() {
        // When & Then
        assertThrows(
                ValidationException.class,
                () -> {
                    throw new ValidationException("Test exception");
                });
    }
}
