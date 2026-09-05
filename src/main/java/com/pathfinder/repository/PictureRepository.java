package com.pathfinder.repository;

import com.pathfinder.model.entity.PictureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PictureRepository extends JpaRepository<PictureEntity, Long> {

    @Query(value = "SELECT p.url FROM pictures p ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<String> findRandomUrls(@Param("limit") int limit);
}
