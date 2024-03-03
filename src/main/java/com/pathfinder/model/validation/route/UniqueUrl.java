package com.pathfinder.model.validation.route;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = UniqueUrlValidator.class)
public @interface UniqueUrl {

    String message() default "{route.video-url.duplication}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
