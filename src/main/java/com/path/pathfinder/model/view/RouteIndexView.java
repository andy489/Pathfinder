package com.path.pathfinder.model.view;

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
}
