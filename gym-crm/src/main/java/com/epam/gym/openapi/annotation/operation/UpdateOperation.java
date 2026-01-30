package com.epam.gym.openapi.annotation.operation;

import com.epam.gym.openapi.annotation.composed.UpdateResponses;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation
@UpdateResponses
public @interface UpdateOperation {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Update resource";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";
}
