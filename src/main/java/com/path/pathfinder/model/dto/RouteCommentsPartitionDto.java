package com.path.pathfinder.model.dto;

import com.path.pathfinder.model.view.CommentView;

import java.util.List;

public record RouteCommentsPartitionDto(Long routeId,
                                        List<CommentView> approvedComments,
                                        List<CommentView> newComments) {
}
