package com.pathfinder.model.validation.route;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

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
        if (file == null || file.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File is required").addConstraintViolation();
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File must have a valid name").addConstraintViolation();
            return false;
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
        if (file.getSize() > size) {
            return "Exceeded file size. Max size: " + size;
        }

        if (!contentTypes.contains(file.getContentType())) {
            return "Invalid file extension. Supported files: " + String.join(", ", contentTypes);
        }

        return null;
    }
}
