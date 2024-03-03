package com.pathfinder.model.dto;

import java.util.List;

public record MapCoordinatesDto(List<List<Double>> coordinates,
                                Double zoom,
                                Double minLon,
                                Double minLat,
                                Double maxLon,
                                Double maxLat) {
}
