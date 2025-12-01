package com.epam.gym.openapi.annotation.base.response.client_error;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponse(
        responseCode = "404",
        description = "Resource not found"
)
public @interface ResponseNotFound_404 {
}
