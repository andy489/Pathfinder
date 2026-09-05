package com.pathfinder.service;

import com.pathfinder.model.entity.CategoryEntity;
import com.pathfinder.model.enumerated.RouteCategoryEnum;
import com.pathfinder.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Set<CategoryEntity> getCategoryEntities(Set<RouteCategoryEnum> routeCategoryEnumSet) {
        return categoryRepository.findAllByCategoryIn(routeCategoryEnumSet);
    }
}
