package com.path.pathfinder.model.validation.route;

import com.path.pathfinder.service.RouteService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueUrlValidator implements ConstraintValidator<UniqueUrl, String> {

    private final RouteService routeService;

    public UniqueUrlValidator(RouteService routeService) {
        this.routeService = routeService;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value.isEmpty() || value.isBlank()) {
            return true;
        }

        return routeService.getByVideoUrl(value).isEmpty();
    }
}
