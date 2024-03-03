package com.path.pathfinder.service;

import com.path.pathfinder.model.entity.CategoryEntity;
import com.path.pathfinder.model.enumerated.RouteCategoryEnum;
import com.path.pathfinder.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    CategoryEntity getCategoryEntity(RouteCategoryEnum routeCategoryEnum) {
        return categoryRepository.findByCategory(routeCategoryEnum).orElseThrow(NoSuchFieldError::new);
    }

    public Set<CategoryEntity> getCategoryEntities(Set<RouteCategoryEnum> routeCategoryEnumSet) {

        return routeCategoryEnumSet.stream()
                .map(this::getCategoryEntity)
                .collect(Collectors.toSet());
    }
}
