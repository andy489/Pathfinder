package com.pathfinder.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class PathfinderControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(PathfinderControllerAdvice.class);

    private final String mapboxToken;
    private final List<String> weatherCities;

    public PathfinderControllerAdvice(
            @Value("${pathfinder.mapbox-token:}") String mapboxToken,
            @Value("${pathfinder.weather.cities:Sofia,Sozopol}") String weatherCities) {
        this.mapboxToken = mapboxToken;
        this.weatherCities = Arrays.stream(weatherCities.split(","))
                .map(String::trim)
                .toList();
    }

    @ModelAttribute("mapboxToken")
    public String mapboxToken() {
        return mapboxToken;
    }

    @ModelAttribute("weatherCities")
    public List<String> weatherCities() {
        return weatherCities;
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Exception ex) {
        if (ex instanceof AccessDeniedException ade) throw ade;
        if (ex instanceof AuthenticationException ae) throw ae;
        log.error("Unhandled exception", ex);
        return "error";
    }
}
