package com.pathfinder.repository;

import com.pathfinder.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String userName);

    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.roles")
    List<UserEntity> findAllWithRoles();
}

