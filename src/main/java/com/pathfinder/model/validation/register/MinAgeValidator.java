package com.pathfinder.model.validation.register;

import com.pathfinder.util.DateUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Date;
public class MinAgeValidator implements ConstraintValidator<MinAge, Date> {

    @Value("${user.register.min-age}")
    private Integer minAge;

    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        return DateUtil.calcYearsBetween(value, LocalDate.now()) > minAge;
    }

}
