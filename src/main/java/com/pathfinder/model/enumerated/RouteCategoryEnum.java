package com.pathfinder.model.enumerated;

public enum RouteCategoryEnum {

    PEDESTRIAN("Pedestrian"),
    BICYCLE("Bicycle"),
    MOTORCYCLE("Motorcycle"),
    CAR("Car");

    final String displayName;

    RouteCategoryEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
