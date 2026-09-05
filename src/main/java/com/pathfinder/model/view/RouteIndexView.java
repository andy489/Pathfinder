package com.pathfinder.model.view;

import com.pathfinder.model.enumerated.LevelEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class RouteIndexView {

    private Long id;

    private String name;

    private String description;

    private String pictureUrl;

    private LevelEnum level;
}
