package com.epam.gym.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NotFoundExceptionTest {

  @Test
  void constructor_withMessage_setsMessage() {
    // Given
    String message = "Resource not found";

    // When
    NotFoundException exception = new NotFoundException(message);

    // Then
    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void constructor_withNullMessage_handlesGracefully() {
    // When
    NotFoundException exception = new NotFoundException(null);

    // Then
    assertNull(exception.getMessage());
  }

  @Test
  void exception_canBeThrown() {
    // When & Then
    assertThrows(
        NotFoundException.class,
        () -> {
          throw new NotFoundException("Test exception");
        });
  }
}
