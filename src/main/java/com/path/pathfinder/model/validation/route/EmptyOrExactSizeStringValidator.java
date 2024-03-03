package com.path.pathfinder.model.validation.route;

import jakarta.validation.ConstraintValidator;

import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

public class EmptyOrExactSizeStringValidator implements ConstraintValidator<EmptyOrExactSizeString, String> {

    @Value("${route.youtube.code.size}")
    private int exactSize;

    @Override
    public void initialize(EmptyOrExactSizeString constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);

        this.exactSize = constraintAnnotation.exactSize();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value.isEmpty() || value.length() == exactSize;
    }
}