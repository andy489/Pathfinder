package com.pathfinder.service;

import com.pathfinder.mapper.MapStructMapper;
import com.pathfinder.model.dto.CommentCreationDto;
import com.pathfinder.model.entity.CommentEntity;
import com.pathfinder.model.entity.RouteEntity;
import com.pathfinder.model.entity.UserEntity;
import com.pathfinder.model.view.CommentView;
import com.pathfinder.repository.CommentRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    private final RouteService routeService;

    private final UserService userService;

    private final MapStructMapper mapper;

    public CommentService(CommentRepository commentRepository,
                          RouteService routeService,
                          UserService userService,
                          MapStructMapper mapper) {
        this.commentRepository = commentRepository;
        this.routeService = routeService;
        this.userService = userService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CommentView> getAllCommentsForRoute(Long routeId) {
        return commentRepository.findApprovedByRouteId(routeId).stream()
                .map(mapper::toView)
                .toList();
    }

    @Transactional
    public CommentView createComment(CommentCreationDto commentDto) {
        UserEntity author = userService.getByUsername(commentDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Failed to find user with username %s", commentDto.getUsername())));

        RouteEntity route = routeService.getById(commentDto.getRouteId())
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("Failed to find route with id %d", commentDto.getRouteId())));

        CommentEntity commentEntity = mapper.toEntity(commentDto);
        commentEntity.setRoute(route);
        commentEntity.setAuthor(author);

        commentEntity = commentRepository.save(commentEntity);

        return mapper.toView(commentEntity);
    }

    @Transactional(readOnly = true)
    public Optional<CommentEntity> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Transactional
    public void toggleApproveComment(Long commentId) {
        commentRepository.getReferenceById(commentId).toggleApprove();
    }
}
