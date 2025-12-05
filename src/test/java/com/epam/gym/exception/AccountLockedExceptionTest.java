package com.epam.gym.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AccountLockedExceptionTest {

  @Test
  void constructor_withMessage_shouldSetMessage() {
    // Given
    String errorMessage = "Account is locked";

    // When
    AccountLockedException exception = new AccountLockedException(errorMessage);

    // Then
    assertNotNull(exception);
    assertEquals(errorMessage, exception.getMessage());
  }

  @Test
  void constructor_withDetailedMessage_shouldSetMessage() {
    // Given
    String errorMessage =
        "Account is locked due to too many failed login attempts. Please try again in 5 minutes.";

    // When
    AccountLockedException exception = new AccountLockedException(errorMessage);

    // Then
    assertEquals(errorMessage, exception.getMessage());
  }

  @Test
  void exception_isRuntimeException_shouldBeThrowableWithoutDeclaration() {
    // Given/When/Then
    assertThrows(
        AccountLockedException.class,
        () -> {
          throw new AccountLockedException("Test");
        });
  }

  @Test
  void exception_canBeCaught_shouldBeCatchable() {
    // Given
    String message = "Account locked";
    boolean exceptionCaught = false;

    // When
    try {
      throw new AccountLockedException(message);
    } catch (AccountLockedException e) {
      // Then
      assertEquals(message, e.getMessage());
      exceptionCaught = true;
    }

    assertTrue(exceptionCaught, "Exception should have been caught");
  }

  @Test
  void exception_extendsRuntimeException_shouldBeInstanceOfRuntimeException() {
    // Given
    AccountLockedException exception = new AccountLockedException("Test");

    // When/Then
    assertInstanceOf(RuntimeException.class, exception);
  }

  @Test
  void exception_withNullMessage_shouldAllowNullMessage() {
    // When
    AccountLockedException exception = new AccountLockedException(null);

    // Then
    assertNull(exception.getMessage());
  }

  @Test
  void exception_withEmptyMessage_shouldAllowEmptyMessage() {
    // When
    AccountLockedException exception = new AccountLockedException("");

    // Then
    assertEquals("", exception.getMessage());
  }

  @Test
  void exception_canBeRethrown_shouldPreserveMessage() {
    // Given
    String originalMessage = "Account locked for security";
    boolean exceptionCaught = false;

    // When
    try {
      try {
        throw new AccountLockedException(originalMessage);
      } catch (AccountLockedException e) {
        throw e; // Rethrow
      }
    } catch (AccountLockedException caught) {
      // Then
      assertEquals(originalMessage, caught.getMessage());
      exceptionCaught = true;
    }

    assertTrue(exceptionCaught, "Exception should have been caught");
  }
}
