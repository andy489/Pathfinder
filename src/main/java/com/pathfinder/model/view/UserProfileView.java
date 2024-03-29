package com.pathfinder.model.view;

import com.pathfinder.model.entity.RoleEntity;
import com.pathfinder.model.enumerated.LevelEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
public class UserProfileView {

    private Long id;

    private String fullName;

    private String username;

    private String password;

    private String email;

    private Integer age;

    private LevelEnum level;

    private Set<RoleEntity> roles;
}
