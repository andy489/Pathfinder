package com.pathfinder.model.validation.register;

import com.pathfinder.service.UserService;
import jakarta.validation.ConstraintValidator;

import jakarta.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private final UserService userService;

    public UniqueUsernameValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return userService.getByUsername(value).isEmpty();
    }
}
