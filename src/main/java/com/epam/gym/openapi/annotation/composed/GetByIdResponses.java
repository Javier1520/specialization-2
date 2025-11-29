package com.epam.gym.openapi.annotation.composed;

import com.epam.gym.openapi.annotation.base.response.client_error.ResponseNotFound_404;
import com.epam.gym.openapi.annotation.base.response.server_error.ResponseInternalServerError_500;
import com.epam.gym.openapi.annotation.base.response.successful.ResponseOk_200;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ResponseOk_200
@ResponseNotFound_404
@ResponseInternalServerError_500
public @interface GetByIdResponses {
}
