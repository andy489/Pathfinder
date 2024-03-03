package com.path.pathfinder.model.entity;

import com.path.pathfinder.model.enumerated.LevelEnum;
import com.path.pathfinder.model.enumerated.UserRoleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Accessors(chain = true)
public class UserEntity extends GenericEntity {

    @Column(nullable = false, unique = true)
    private String username;

    private String fullName;

    @Column(nullable = false)
    private Date birthDate;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns =
            @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns =
            @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LevelEnum level = LevelEnum.BEGINNER;

    public UserEntity addRole(RoleEntity roleEntity) {
        roles.add(roleEntity);
        return this;
    }

    public boolean containsRole(UserRoleEnum roleEnum) {

        for (RoleEntity next : roles) {

            if (next.getRole().equals(roleEnum)) {
                return true;
            }
        }

        return false;
    }

    public void remove(UserRoleEnum userRoleEnum) {

        roles.removeIf(r -> r.getRole().equals(userRoleEnum));
    }
}
