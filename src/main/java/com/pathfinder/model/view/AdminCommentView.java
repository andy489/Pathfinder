package com.pathfinder.model.view;

import com.pathfinder.model.dto.RouteCommentsPartitionDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class AdminCommentView {

    private Long newCommentsCount;

    private Long approvedCommentsCount;

    private Map<String, RouteCommentsPartitionDto> routeComments;
}
