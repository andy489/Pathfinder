package com.pathfinder.repository;

import com.pathfinder.model.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

    Optional<RouteEntity> findByVideoUrl(String videoUrl);
}
