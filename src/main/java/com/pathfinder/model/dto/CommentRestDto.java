package com.pathfinder.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class CommentRestDto {

    @NotNull
    private Long routeId;

    @NotBlank
    @Size(min = 10, max = 2000)
    private String comment;
}
