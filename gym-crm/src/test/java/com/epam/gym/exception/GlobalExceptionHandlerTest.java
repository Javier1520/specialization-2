package com.epam.gym.exception;

import com.epam.gym.util.LogUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock private LogUtils logUtils;

    @InjectMocks private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleNotFoundException_returnsNotFoundStatus() {
        // Given
        NotFoundException exception = new NotFoundException("Resource not found");

        // When
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleNotFoundException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().get("errors"));
    }

    @Test
    void handleValidationException_returnsBadRequestStatus() {
        // Given
        ValidationException exception = new ValidationException("Invalid input");

        // When
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleValidationException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid input", response.getBody().get("errors"));
    }

    @Test
    void handleConstraintViolation_returnsBadRequestWithErrors() {
        // Given
        ConstraintViolationException exception =
                new ConstraintViolationException(Set.of(createMockConstraintViolation()));

        // When
        ResponseEntity<Map<String, Object>> response =
                globalExceptionHandler.handleConstraintViolation(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("errors"));
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().get("errors"));
    }

    @Test
    void handleGenericException_withUnhandledException_returnsInternalServerError() {
        // Given - use an exception that has NO dedicated handler
        Exception exception = new IllegalStateException("Unexpected state");

        // When
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().get("errors"));
    }

    @Test
    void handleNullPointerException_returnsNotFoundStatus() {
        // Given
        NullPointerException exception = new NullPointerException("Null pointer");

        // When
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleNullPointerException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().get("errors"));
    }

    private ConstraintViolation<?> createMockConstraintViolation() {
        return new jakarta.validation.ConstraintViolation<Object>() {
            @Override
            public String getMessage() {
                return "Constraint violation message";
            }

            @Override
            public String getMessageTemplate() {
                return "template";
            }

            @Override
            public Object getRootBean() {
                return null;
            }

            @Override
            public Class<Object> getRootBeanClass() {
                return Object.class;
            }

            @Override
            public Object getLeafBean() {
                return null;
            }

            @Override
            public Object[] getExecutableParameters() {
                return new Object[0];
            }

            @Override
            public Object getExecutableReturnValue() {
                return null;
            }

            @Override
            public jakarta.validation.Path getPropertyPath() {
                return new jakarta.validation.Path() {
                    @Override
                    public String toString() {
                        return "fieldName";
                    }

                    @Override
                    public Iterator<Node> iterator() {
                        return Collections.emptyIterator();
                    }
                };
            }

            @Override
            public Object getInvalidValue() {
                return null;
            }

            @Override
            public jakarta.validation.metadata.ConstraintDescriptor<?> getConstraintDescriptor() {
                return null;
            }

            @Override
            public <U> U unwrap(Class<U> type) {
                return null;
            }
        };
    }
}
