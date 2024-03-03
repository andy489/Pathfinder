package com.path.pathfinder.service;

import com.path.pathfinder.mapper.MapStructMapper;
import com.path.pathfinder.model.user.PathfinderUserDetails;
import com.path.pathfinder.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

// NOTE: This is not annotated as @Service, because we will return it as a bean.
public class PathfinderUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final MapStructMapper mapper;

    public PathfinderUserDetailsService(UserRepository userRepository, MapStructMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }


    @Override
    public PathfinderUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<PathfinderUserDetails> pathfinderUserDetails = userRepository.findByUsername(username)
                .map(u -> {
                    PathfinderUserDetails userDetails = mapper.toUserDetails(u);

                    userDetails.setAuthorities(u.getRoles().stream().map(mapper::toGrantedAuthority)
                            .toList());

                    return userDetails;
                });

        return pathfinderUserDetails
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
    }
}
