package com.path.pathfinder.model.view;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
public class RouteWithCategoryView extends RouteIndexView {

    private Set<String> categoryTypes;
}
