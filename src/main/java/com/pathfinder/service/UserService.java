package com.pathfinder.service;

import com.pathfinder.mapper.MapStructMapper;
import com.pathfinder.model.dto.UserRegistrationDto;
import com.pathfinder.model.entity.UserEntity;
import com.pathfinder.model.enumerated.LevelEnum;
import com.pathfinder.model.enumerated.UserRoleEnum;
import com.pathfinder.model.view.UserProfileView;
import com.pathfinder.repository.RoleRepository;
import com.pathfinder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class UserService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final MapStructMapper mapper;

    private final PasswordEncoder encoder;

    private final UserDetailsService userDetailsService;


    @Autowired
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            MapStructMapper mapper,
            UserDetailsService userDetailsService,
            PasswordEncoder encoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
        this.userDetailsService = userDetailsService;
        this.encoder = encoder;
    }

    public void registerAndLogin(UserRegistrationDto userRegistrationDto,
                                 Consumer<Authentication> successfulLoginProcessor) {

        UserEntity newUser = mapper.toEntity(userRegistrationDto)
                .setPassword(encoder.encode(userRegistrationDto.getPassword()))
                .setLevel(LevelEnum.BEGINNER)
                .addRole(roleRepository.findRoleEntityByRole(UserRoleEnum.REGULAR).orElseThrow(NoSuchElementException::new));

        userRepository.save(newUser);

        // auto-login
        UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, // principal
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        successfulLoginProcessor.accept(authentication);
    }

    public UserProfileView getById(Long id) {
        return mapper.toView(userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No such user (getById)")));
    }

    public Optional<UserEntity> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<UserEntity> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}

