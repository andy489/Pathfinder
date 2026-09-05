package com.pathfinder.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ErrorApiResponse {

    public static final int ROUTE_NOT_FOUND = 1001;

    private String message;

    private Integer errorCode;
}
