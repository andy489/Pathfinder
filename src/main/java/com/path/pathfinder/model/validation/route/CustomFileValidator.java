package com.path.pathfinder.model.validation.route;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CustomFileValidator implements ConstraintValidator<CustomFile, MultipartFile> {

    private Long size;

    private List<String> contentTypes;


    @Override
    public void initialize(CustomFile constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);

        this.size = constraintAnnotation.size();
        this.contentTypes = Arrays.stream(constraintAnnotation.contentTypes()).toList();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {

        if (Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
            return true;
        }

        String errMsg = getErrMsg(file);

        if (errMsg != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errMsg).addConstraintViolation();

            return false;
        }

        return true;
    }

    private String getErrMsg(MultipartFile file) {
        if (file.isEmpty()) {
            return "file must not be empty";
        }

        if (file.getSize() > size) {
            return "Exceeded file size. Max size: " + size;
        }

        if (!contentTypes.contains(file.getContentType())) {
            return "Invalid file extension. Supported files: " + String.join(", ", contentTypes);
        }

        return null;
    }
}
