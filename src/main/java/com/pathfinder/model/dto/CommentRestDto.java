package com.pathfinder.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class CommentRestDto {

    private Long routeId;

    private String comment;

}
