package com.pathfinder.repository;

import com.pathfinder.model.entity.RoleEntity;
import com.pathfinder.model.enumerated.UserRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findRoleEntityByRole(UserRoleEnum userRoleEnum);
}
