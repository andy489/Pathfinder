package com.pathfinder.web.rest;

import com.pathfinder.exception.RouteNotFoundException;
import com.pathfinder.model.dto.CommentCreationDto;
import com.pathfinder.model.dto.CommentRestDto;
import com.pathfinder.model.user.PathfinderUserDetails;
import com.pathfinder.model.view.CommentView;
import com.pathfinder.service.CommentService;
import com.pathfinder.web.GenericController;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentRestController extends GenericController {

    private final CommentService commentService;

    @Autowired
    public CommentRestController(CommentService commentService) {

        this.commentService = commentService;
    }

    @GetMapping("/{routeId}/comments")
    public ResponseEntity<List<CommentView>> allForRoute(@PathVariable("routeId") Long routeId) {

        return ResponseEntity.ok(commentService.getAllCommentsForRoute(routeId));
    }


    @PostMapping(path ="/comment",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CommentView> create(@AuthenticationPrincipal PathfinderUserDetails userDetails,
                                              @RequestBody CommentRestDto commentRestDto) {

        CommentCreationDto commentCreationDto = new CommentCreationDto()
                .setUsername(userDetails.getUsername())
                .setComment(commentRestDto.getComment())
                .setRouteId(commentRestDto.getRouteId());

        CommentView commentView = commentService.createComment(commentCreationDto);

        return ResponseEntity.created(
                URI.create(String.format(
                                "/api/%d/comments/%d",
                                commentRestDto.getRouteId(),
                                commentView.getCommentId()
                        )
                )).body(commentView);
    }

    @ExceptionHandler({RouteNotFoundException.class})
    public ResponseEntity<ErrorApiResponse> handleRouteNotFoundException() {
        return ResponseEntity
                .status(404)
                .body(new ErrorApiResponse().setMessage("Failed to find route.").setErrorCode(1234));
    }
}

@Getter
@Setter
@Accessors(chain = true)
class ErrorApiResponse {

    private String message;

    private Integer errorCode;
}

