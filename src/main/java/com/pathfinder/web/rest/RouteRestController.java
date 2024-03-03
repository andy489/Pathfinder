package com.pathfinder.web.rest;

import com.pathfinder.model.dto.MapCoordinatesDto;
import com.pathfinder.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteRestController {

    private final RouteService routeService;

    public RouteRestController(RouteService routeService) {

        this.routeService = routeService;
    }

    @GetMapping("/api/routes/coordinates/{id}")
    private ResponseEntity<MapCoordinatesDto> getRouteCoordinates(@PathVariable("id") Long routeId) {

        return ResponseEntity.ok(routeService.getCoordinates(routeId));

    }

}
