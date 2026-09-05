package com.pathfinder.model.validation;

import com.pathfinder.model.validation.register.MinAgeValidator;
import com.pathfinder.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MinAgeValidatorTest {

    private MinAgeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MinAgeValidator();
        ReflectionTestUtils.setField(validator, "minAge", 12);
    }

    @Test
    void nullDateIsValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void exactMinAgeIsValid() {
        LocalDate exactlyTwelve = LocalDate.now().minusYears(12);
        assertThat(validator.isValid(exactlyTwelve, null)).isTrue();
    }

    @Test
    void aboveMinAgeIsValid() {
        assertThat(validator.isValid(LocalDate.now().minusYears(18), null)).isTrue();
    }

    @Test
    void belowMinAgeIsInvalid() {
        LocalDate elevenYearsOld = LocalDate.now().minusYears(11).plusDays(1);
        assertThat(validator.isValid(elevenYearsOld, null)).isFalse();
    }

    @Test
    void futureDateIsInvalid() {
        assertThat(validator.isValid(LocalDate.now().plusDays(1), null)).isFalse();
    }
}
