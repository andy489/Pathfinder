package com.pathfinder.model.dto;

import com.pathfinder.model.view.CommentView;

import java.util.List;

public record RouteCommentsPartitionDto(Long routeId,
                                        List<CommentView> approvedComments,
                                        List<CommentView> newComments) {
}
