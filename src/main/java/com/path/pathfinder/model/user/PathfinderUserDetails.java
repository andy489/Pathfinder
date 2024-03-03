package com.path.pathfinder.model.user;

import com.path.pathfinder.model.enumerated.LevelEnum;
import com.path.pathfinder.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PathfinderUserDetails implements UserDetails {

    private Long id;

    private String username;

    private String password;

    private String email;

    private Date birthDate;

    private String fullName;

    private LevelEnum level;

    private Collection<GrantedAuthority> authorities;

    public Integer getAge() {
        if(birthDate == null){
            throw new IllegalStateException("birthDate is not set");
        }

        return DateUtil.calcYearsBetween(birthDate, LocalDate.now());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
