package com.epam.gym.workload.exception;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleValidationExceptions_returnsBadRequestWithErrors() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "field1", "message1"));

        MethodParameter mockParameter = mock(MethodParameter.class);
        when(mockParameter.getParameterIndex()).thenReturn(-1);
        when(mockParameter.getExecutable()).thenReturn(
                GlobalExceptionHandlerTest.class.getDeclaredMethods()[0]
        );

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(mockParameter, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("message1", response.getBody().get("field1"));
    }

    @Test
    void handleGlobalException_returnsInternalServerErrorWithGenericMessage() {
        Exception ex = mock(Exception.class);

        ResponseEntity<Map<String, String>> response = handler.handleGlobalException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }

    @Test
    void handleNullPointerException_returnsInternalServerErrorWithoutInternalDetails() {
        NullPointerException ex = mock(NullPointerException.class);

        ResponseEntity<Map<String, String>> response = handler.handleNullPointerException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }
}