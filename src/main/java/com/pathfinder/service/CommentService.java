package com.pathfinder.service;

import com.pathfinder.mapper.MapStructMapper;
import com.pathfinder.model.dto.CommentCreationDto;
import com.pathfinder.model.entity.CommentEntity;
import com.pathfinder.model.entity.RouteEntity;
import com.pathfinder.model.entity.UserEntity;
import com.pathfinder.model.view.CommentView;
import com.pathfinder.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    private final RouteService routeService;

    private final UserService userService;

    private final MapStructMapper mapper;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          RouteService routeService,
                          UserService userService,
                          MapStructMapper mapper) {
        this.commentRepository = commentRepository;
        this.routeService = routeService;
        this.userService = userService;
        this.mapper = mapper;
    }


    public List<CommentView> getAllCommentsForRoute(Long routeId) {
        List<CommentView> commentViews = routeService.getById(routeId)
                .map(route -> commentRepository.findAllByRoute(route).stream()
                        .filter(CommentEntity::getApproved)
                        .map(mapper::toView)
                        .sorted(Comparator.comparing(CommentView::getCreated, Comparator.reverseOrder()))
                        .toList())
                .orElse(Collections.emptyList());

        return commentViews;
    }

    public CommentView createComment(CommentCreationDto commentDto) {

        UserEntity author = userService.findByUsername(commentDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Failed to find user with username %s", commentDto.getUsername()))
                );

        RouteEntity route = routeService.getById(commentDto.getRouteId())
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("Failed to find route with id %d", commentDto.getRouteId()))
                );

        CommentEntity commentEntity = mapper.toEntity(commentDto);
        commentEntity.setRoute(route);
        commentEntity.setAuthor(author);


        commentEntity = commentRepository.save(commentEntity);

        return mapper.toView(commentEntity);
    }

    public Optional<CommentEntity> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Transactional
    public void toggleApproveComment(Long commentId) {
        commentRepository.getReferenceById(commentId).toggleApprove();
    }
}
