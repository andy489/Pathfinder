package com.pathfinder.model.validation.route;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CustomFileValidator.class})
public @interface CustomFile {

    long size() default 5L * 1024 * 1024;

    String[] contentTypes();

    String message() default "{route.gpx.invalid.format}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
