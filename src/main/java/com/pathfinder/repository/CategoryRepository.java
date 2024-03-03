package com.pathfinder.repository;

import com.pathfinder.model.entity.CategoryEntity;
import com.pathfinder.model.enumerated.RouteCategoryEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByCategory(RouteCategoryEnum routeCategoryEnum);
}
