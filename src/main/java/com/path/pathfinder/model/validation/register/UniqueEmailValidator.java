package com.path.pathfinder.model.validation.register;

import com.path.pathfinder.service.UserService;
import jakarta.validation.ConstraintValidator;

import jakarta.validation.ConstraintValidatorContext;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final UserService userService;

    public UniqueEmailValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return userService.getByEmail(value).isEmpty();
    }
}