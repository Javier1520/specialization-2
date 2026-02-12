package com.epam.gym.exception;

import com.epam.gym.util.LogUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERRORS = "errors";
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final LogUtils logUtils;

    public GlobalExceptionHandler(LogUtils logUtils) {
        this.logUtils = logUtils;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
        logUtils.error(log, "Not found exception: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException ex) {
        logUtils.error(log, "Validation exception: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountLockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Map<String, String>> handleAccountLockedException(
            AccountLockedException ex) {
        logUtils.error(log, "Account locked exception: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        logUtils.error(log, "Validation error: {}", ex.getMessage());
        return buildMethodArgumentNotValidErrorResponse(ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex) {
        logUtils.error(log, "Constraint violation: {}", ex.getMessage());
        return buildConstraintViolationErrorResponse(ex);
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleNullPointerException(NullPointerException ex) {
        logUtils.error(log, "Null pointer exception: {}", ex.getMessage());
        return buildErrorResponse("Requested resource not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        logUtils.error(log, "Unexpected error: ", ex);
        return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String ex, HttpStatus httpStatus) {
        Map<String, String> error = new HashMap<>();
        error.put(ERRORS, ex);
        return ResponseEntity.status(httpStatus).body(error);
    }

    private void mapErrors(FieldError fieldError, Map<String, Object> errors) {
        String fieldName = fieldError.getField();
        String errorMessage = fieldError.getDefaultMessage();
        errors.put(fieldName, errorMessage);
    }

    private ResponseEntity<Map<String, Object>> buildConstraintViolationErrorResponse(
            ConstraintViolationException ex) {
        Map<String, Object> errors =
                ex.getConstraintViolations().stream()
                        .collect(
                                Collectors.toMap(
                                        violation -> violation.getPropertyPath().toString(),
                                        ConstraintViolation::getMessage));
        Map<String, Object> response = new HashMap<>();
        response.put(ERRORS, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private ResponseEntity<Map<String, Object>> buildMethodArgumentNotValidErrorResponse(
            MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .forEach(fieldError -> mapErrors(fieldError, errors));
        Map<String, Object> response = new HashMap<>();
        response.put(ERRORS, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
