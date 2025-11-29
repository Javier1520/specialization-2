package com.epam.gym.openapi.annotation.operation;

import com.epam.gym.openapi.annotation.composed.GetAllResponses;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation
@GetAllResponses
public @interface GetAllOperation {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Get all resources";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";
}
