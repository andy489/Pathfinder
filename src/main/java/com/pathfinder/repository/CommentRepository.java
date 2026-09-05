package com.pathfinder.repository;

import com.pathfinder.model.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query("SELECT c FROM CommentEntity c WHERE c.route.id = :routeId AND c.approved = true ORDER BY c.created DESC")
    List<CommentEntity> findApprovedByRouteId(@Param("routeId") Long routeId);

    @Query("SELECT c.id FROM CommentEntity c WHERE c.route.id = :routeId AND c.approved = :approved")
    List<Long> findIdsByRouteIdAndApproved(@Param("routeId") Long routeId, @Param("approved") Boolean approved);

    @Modifying
    @Query("DELETE FROM CommentEntity c WHERE c.route.id = :routeId AND c.approved = :approved")
    int deleteByRouteIdAndApproved(@Param("routeId") Long routeId, @Param("approved") Boolean approved);
}
