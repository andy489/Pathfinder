package com.pathfinder.service;

import com.pathfinder.mapper.MapStructMapper;
import com.pathfinder.model.dto.UserEditDto;
import com.pathfinder.model.entity.UserEntity;
import com.pathfinder.repository.RoleRepository;
import com.pathfinder.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private MapStructMapper mapper;
    @Mock private UserDetailsService userDetailsService;
    @Mock private PasswordEncoder passwordEncoder;

    private UserService userService;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, mapper, userDetailsService, passwordEncoder);
        user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("old@example.com");
        user.setFullName("Old Name");
        user.setUsername("testuser");
        user.setBirthDate(java.time.LocalDate.of(1990, 1, 1));
    }

    @Test
    void updateProfile_changesNameAndEmail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        UserEditDto dto = new UserEditDto()
                .setFullName("New Name")
                .setEmail("new@example.com");

        userService.updateProfile(1L, dto);

        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_emailTakenByOtherUser_throwsIllegalArgument() {
        UserEntity other = new UserEntity();
        ReflectionTestUtils.setField(other, "id", 2L);
        other.setEmail("taken@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        UserEditDto dto = new UserEditDto()
                .setFullName("Name")
                .setEmail("taken@example.com");

        assertThatThrownBy(() -> userService.updateProfile(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void updateProfile_sameEmailAllowed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("old@example.com")).thenReturn(Optional.of(user));

        UserEditDto dto = new UserEditDto()
                .setFullName("Same")
                .setEmail("old@example.com");

        userService.updateProfile(1L, dto);

        verify(userRepository).save(user);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateProfile_newPasswordIsEncoded() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        UserDetails stubDetails = new org.springframework.security.core.userdetails.User(
                "testuser", "encoded", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(stubDetails);

        UserEditDto dto = new UserEditDto()
                .setFullName("Name")
                .setEmail("email@example.com")
                .setNewPassword("newpass");

        userService.updateProfile(1L, dto);

        verify(passwordEncoder).encode("newpass");
        verify(userRepository).save(user);
    }
}
