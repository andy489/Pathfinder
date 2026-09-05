package com.pathfinder.model.validation.register;

import com.pathfinder.util.DateUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {

    @Value("${user.register.min-age}")
    private Integer minAge;

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return DateUtil.calcYearsBetween(value, LocalDate.now()) >= minAge;
    }

}
