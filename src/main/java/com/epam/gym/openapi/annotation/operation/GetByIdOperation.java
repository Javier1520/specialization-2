package com.epam.gym.openapi.annotation.operation;

import com.epam.gym.openapi.annotation.composed.GetByIdResponses;
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
@GetByIdResponses
public @interface GetByIdOperation {
  @AliasFor(annotation = Operation.class, attribute = "summary")
  String summary() default "Get resource by ID";

  @AliasFor(annotation = Operation.class, attribute = "description")
  String description() default "";
}
