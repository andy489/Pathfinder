package com.pathfinder.model.view;

import com.pathfinder.model.enumerated.LevelEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
public class RouteDetailsView {

    private Long id;

    private String gpxCoordinates;

    private LevelEnum level;

    private String name;

    private String description;

    private String videoUrl;

    private Set<String> pictureUrls;

    private String authorName;
}
