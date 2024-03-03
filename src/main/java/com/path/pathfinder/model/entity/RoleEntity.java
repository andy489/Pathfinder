package com.path.pathfinder.model.entity;

import com.path.pathfinder.model.enumerated.UserRoleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Accessors(chain = true)
public class RoleEntity extends GenericEntity {

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private UserRoleEnum role;
}
