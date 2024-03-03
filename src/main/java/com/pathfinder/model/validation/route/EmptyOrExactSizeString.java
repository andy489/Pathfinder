package com.pathfinder.model.validation.route;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {EmptyOrExactSizeStringValidator.class})
public @interface EmptyOrExactSizeString {

    int exactSize() default 0;

    String message() default "{route.video-url.empty-or-exact-size}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
