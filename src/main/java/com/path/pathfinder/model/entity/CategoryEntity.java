package com.path.pathfinder.model.entity;

import com.path.pathfinder.model.enumerated.RouteCategoryEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "categories")
@Getter
@Setter
@Accessors(chain = true)
public class CategoryEntity extends GenericEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteCategoryEnum category;

    @Column(columnDefinition = "TEXT")
    private String description;
}
