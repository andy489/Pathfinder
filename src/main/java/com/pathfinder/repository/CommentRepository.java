package com.pathfinder.repository;

import com.pathfinder.model.entity.CommentEntity;
import com.pathfinder.model.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findAllByRoute(RouteEntity route);

    @Query(value = "SELECT route_id FROM comments c " +
            "WHERE c.approved = true " +
            "GROUP BY route_id " +
            "ORDER BY COUNT(id) DESC " +
            "LIMIT 1 ", nativeQuery = true)
    Long getMostCommentedRouteId();
}
