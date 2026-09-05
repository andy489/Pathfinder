package com.pathfinder.repository;

import com.pathfinder.model.entity.RouteEntity;
import com.pathfinder.model.enumerated.RouteCategoryEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

    Optional<RouteEntity> findByVideoUrl(String videoUrl);

    @Query("SELECT r FROM RouteEntity r LEFT JOIN FETCH r.pictures LEFT JOIN FETCH r.author WHERE r.id = :id")
    Optional<RouteEntity> findByIdWithPicturesAndAuthor(@Param("id") Long id);

    @Query("SELECT DISTINCT r FROM RouteEntity r LEFT JOIN FETCH r.pictures")
    List<RouteEntity> findAllWithPictures();

    @Query(value = "SELECT DISTINCT r FROM RouteEntity r LEFT JOIN FETCH r.pictures",
           countQuery = "SELECT COUNT(DISTINCT r) FROM RouteEntity r")
    Page<RouteEntity> findAllWithPicturesPaged(Pageable pageable);

    @Query("SELECT DISTINCT r FROM RouteEntity r LEFT JOIN FETCH r.pictures LEFT JOIN FETCH r.comments LEFT JOIN FETCH r.categories")
    List<RouteEntity> findAllWithPicturesAndCommentsAndCategories();

    @Query("SELECT DISTINCT r FROM RouteEntity r LEFT JOIN FETCH r.comments")
    List<RouteEntity> findAllWithComments();

    @Query("""
            SELECT DISTINCT r FROM RouteEntity r
            LEFT JOIN FETCH r.pictures
            LEFT JOIN FETCH r.categories
            WHERE r IN (
                SELECT r2 FROM RouteEntity r2 JOIN r2.categories c2 WHERE c2.category = :category
            )
            """)
    List<RouteEntity> findAllWithPicturesAndCategoriesByCategory(@Param("category") RouteCategoryEnum category);

    @Query("""
            SELECT r.id FROM RouteEntity r
            JOIN r.comments c
            WHERE c.approved = true
            GROUP BY r.id
            ORDER BY COUNT(c) DESC
            """)
    List<Long> findTopCommentedRouteIds(Pageable pageable);

    @Query("SELECT DISTINCT r FROM RouteEntity r LEFT JOIN FETCH r.pictures WHERE r.id IN :ids")
    List<RouteEntity> findByIdsWithPictures(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT r FROM RouteEntity r LEFT JOIN FETCH r.pictures WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<RouteEntity> searchByNameOrDescription(@Param("query") String query);

    @Query("SELECT DISTINCT r FROM RouteEntity r LEFT JOIN FETCH r.pictures ORDER BY r.id ASC LIMIT 1")
    Optional<RouteEntity> findFirstWithPictures();
}
