package com.path.pathfinder.model.validation.register;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = MinAgeValidator.class)
public @interface MinAge {

    String message() default "{user.birth-date.min-age}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
