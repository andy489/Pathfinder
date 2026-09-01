package com.pathfinder.service;

import com.pathfinder.mapper.MapStructMapper;
import com.pathfinder.model.dto.UserEditDto;
import com.pathfinder.model.dto.UserRegistrationDto;
import com.pathfinder.model.entity.UserEntity;
import com.pathfinder.model.enumerated.LevelEnum;
import com.pathfinder.model.enumerated.UserRoleEnum;
import com.pathfinder.repository.RoleRepository;
import com.pathfinder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final MapStructMapper mapper;

    private final PasswordEncoder encoder;

    private final UserDetailsService userDetailsService;

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

    @Transactional
    public void registerAndLogin(UserRegistrationDto userRegistrationDto,
                                 Consumer<Authentication> successfulLoginProcessor) {

        UserEntity newUser = mapper.toEntity(userRegistrationDto)
                .setPassword(encoder.encode(userRegistrationDto.getPassword()))
                .setLevel(LevelEnum.BEGINNER)
                .addRole(roleRepository.findRoleEntityByRole(UserRoleEnum.REGULAR).orElseThrow(NoSuchElementException::new));

        userRepository.save(newUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        successfulLoginProcessor.accept(authentication);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Updates name/email and optionally the password. Returns a fresh {@link Authentication}
     * when the password was changed so the caller can persist the new security context;
     * returns {@code null} when only non-credential fields were updated.
     */
    @Transactional
    public Authentication updateProfile(Long userId, UserEditDto dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        // ensure the new email isn't already taken by a different account
        userRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new IllegalArgumentException("Email already in use");
            }
        });

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            user.setPassword(encoder.encode(dto.getNewPassword()));
            userRepository.save(user);
            UserDetails refreshed = userDetailsService.loadUserByUsername(user.getUsername());
            return new UsernamePasswordAuthenticationToken(
                    refreshed, refreshed.getPassword(), refreshed.getAuthorities());
        }

        userRepository.save(user);
        return null;
    }
}
