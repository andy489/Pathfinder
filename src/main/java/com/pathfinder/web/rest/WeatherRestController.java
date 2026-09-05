package com.pathfinder.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathfinder.model.dto.WeatherDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/weather")
public class WeatherRestController {

    private static final Logger log = LoggerFactory.getLogger(WeatherRestController.class);
    private static final double KELVIN_TO_CELSIUS = -273.15;
    private static final String OWM_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";

    private final String apiKey;
    private final List<String> configuredCities;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WeatherRestController(
            @Value("${pathfinder.weather.api-key:}") String apiKey,
            @Value("${pathfinder.weather.cities:Sofia,Sozopol}") String cities,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.configuredCities = Arrays.asList(cities.split(","));
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<WeatherDto>> getWeather() {
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        List<CompletableFuture<WeatherDto>> futures = configuredCities.stream()
                .map(city -> CompletableFuture.supplyAsync(() -> fetchWeatherForCity(city.trim())))
                .toList();

        List<WeatherDto> results = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(configuredCities.stream().map(String::trim).toList());
    }

    private WeatherDto fetchWeatherForCity(String city) {
        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = String.format(OWM_URL, encodedCity, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Weather API returned {} for city {}", response.statusCode(), city);
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            int tempCelsius = (int) Math.round(root.path("main").path("temp").asDouble() + KELVIN_TO_CELSIUS);
            String icon = root.path("weather").get(0).path("icon").asText();

            return new WeatherDto(city, tempCelsius, icon);
        } catch (IOException | InterruptedException e) {
            log.warn("Failed to fetch weather for city {}: {}", city, e.getMessage());
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return null;
        }
    }
}
