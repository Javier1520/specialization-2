package com.epam.gym.workload.exception;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationExceptions_returnsBadRequestWithErrors() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "field1", "message1"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("message1", response.getBody().get("field1"));
    }

    @Test
    void handleGlobalException_returnsBadRequestWithError() {
        Exception ex = new Exception("test exception");

        ResponseEntity<Map<String, String>> response = handler.handleGlobalException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("test exception"));
    }

    @Test
    void handleNullPointerException_returnsBadRequestWithoutInternalDetails() {
        NullPointerException ex = new NullPointerException("some internal null");

        ResponseEntity<Map<String, String>> response = handler.handleNullPointerException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Requested resource not found", response.getBody().get("error"));
    }
}
