package com.pathfinder.repository;

import com.pathfinder.model.entity.CategoryEntity;
import com.pathfinder.model.enumerated.RouteCategoryEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByCategory(RouteCategoryEnum routeCategoryEnum);

    Set<CategoryEntity> findAllByCategoryIn(Set<RouteCategoryEnum> categories);
}
