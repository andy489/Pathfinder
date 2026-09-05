package com.pathfinder.model.validation;

import com.pathfinder.model.validation.route.CustomFileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;

class CustomFileValidatorTest {

    private CustomFileValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new CustomFileValidator();
        ReflectionTestUtils.setField(validator, "size", 1_048_576L); // 1 MB
        ReflectionTestUtils.setField(validator, "contentTypes",
                java.util.List.of("text/xml", "application/xml", "application/gpx+xml"));

        context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void emptyFileIsInvalid() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xml", "text/xml", new byte[0]);
        assertThat(validator.isValid(file, context)).isFalse();
    }

    @Test
    void nullFileIsInvalid() {
        assertThat(validator.isValid(null, context)).isFalse();
    }

    @Test
    void validXmlFileIsValid() {
        byte[] content = "<gpx/>".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "route.xml", "text/xml", content);
        assertThat(validator.isValid(file, context)).isTrue();
    }

    @Test
    void wrongContentTypeIsInvalid() {
        byte[] content = "data".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", content);
        assertThat(validator.isValid(file, context)).isFalse();
    }

    @Test
    void oversizedFileIsInvalid() {
        byte[] big = new byte[2_000_000];
        MockMultipartFile file = new MockMultipartFile("file", "big.xml", "text/xml", big);
        assertThat(validator.isValid(file, context)).isFalse();
    }
}
