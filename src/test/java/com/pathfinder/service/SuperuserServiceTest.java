package com.pathfinder.service;

import com.pathfinder.exception.RouteNotFoundException;
import com.pathfinder.mapper.MapStructMapper;
import com.pathfinder.model.entity.CommentEntity;
import com.pathfinder.model.entity.RoleEntity;
import com.pathfinder.model.entity.RouteEntity;
import com.pathfinder.model.entity.UserEntity;
import com.pathfinder.model.enumerated.UserRoleEnum;
import com.pathfinder.repository.CommentRepository;
import com.pathfinder.repository.RoleRepository;
import com.pathfinder.repository.RouteRepository;
import com.pathfinder.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperuserServiceTest {

    @Mock private RouteRepository routeRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private MapStructMapper mapper;

    private SuperuserService service;

    @BeforeEach
    void setUp() {
        service = new SuperuserService(routeRepository, userRepository, roleRepository, commentRepository, mapper);
    }

    // ── togglePermUser ────────────────────────────────────────────────────────

    @Test
    void togglePermUser_throwsNoSuchElementForMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.togglePermUser(99L, UserRoleEnum.REGULAR, UserRoleEnum.MODERATOR))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void togglePermUser_promotesToModerator_andKeepsRegular() {
        UserEntity user = buildUserWithRole(UserRoleEnum.REGULAR);

        RoleEntity moderatorRole = roleEntityFor(UserRoleEnum.MODERATOR);
        RoleEntity regularRole = roleEntityFor(UserRoleEnum.REGULAR);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findRoleEntityByRole(UserRoleEnum.MODERATOR)).thenReturn(Optional.of(moderatorRole));
        when(roleRepository.findRoleEntityByRole(UserRoleEnum.REGULAR)).thenReturn(Optional.of(regularRole));

        service.togglePermUser(1L, UserRoleEnum.REGULAR, UserRoleEnum.MODERATOR);

        assertThat(user.containsRole(UserRoleEnum.MODERATOR)).isTrue();
        // REGULAR is re-added when missing
        assertThat(user.containsRole(UserRoleEnum.REGULAR)).isTrue();
    }

    @Test
    void togglePermUser_demotesFromModeratorToRegular() {
        UserEntity user = buildUserWithRole(UserRoleEnum.MODERATOR);
        RoleEntity regularRole = roleEntityFor(UserRoleEnum.REGULAR);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findRoleEntityByRole(UserRoleEnum.REGULAR)).thenReturn(Optional.of(regularRole));

        service.togglePermUser(1L, UserRoleEnum.MODERATOR, UserRoleEnum.REGULAR);

        assertThat(user.containsRole(UserRoleEnum.MODERATOR)).isFalse();
        assertThat(user.containsRole(UserRoleEnum.REGULAR)).isTrue();
    }

    // ── approveRejAllRouteComments ────────────────────────────────────────────

    @Test
    void approveRejAllRouteComments_throwsRouteNotFoundForMissingRoute() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.approveRejAllRouteComments(99L, false))
                .isInstanceOf(RouteNotFoundException.class);
    }

    @Test
    void approveRejAllRouteComments_togglesPendingComments_andReturnsIds() {
        CommentEntity c1 = new CommentEntity();
        ReflectionTestUtils.setField(c1, "id", 10L);
        c1.setApproved(false);

        CommentEntity c2 = new CommentEntity();
        ReflectionTestUtils.setField(c2, "id", 11L);
        c2.setApproved(true); // already approved — should NOT be toggled

        RouteEntity route = new RouteEntity();
        route.getComments().add(c1);
        route.getComments().add(c2);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        List<Long> toggled = service.approveRejAllRouteComments(1L, false);

        assertThat(toggled).containsExactly(10L);
        assertThat(c1.getApproved()).isTrue();
        assertThat(c2.getApproved()).isTrue(); // untouched
    }

    @Test
    void approveRejAllRouteComments_returnsEmptyListWhenNoMatchingComments() {
        CommentEntity c = new CommentEntity();
        ReflectionTestUtils.setField(c, "id", 5L);
        c.setApproved(true);

        RouteEntity route = new RouteEntity();
        route.getComments().add(c);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        List<Long> toggled = service.approveRejAllRouteComments(1L, false); // looking for pending only

        assertThat(toggled).isEmpty();
        assertThat(c.getApproved()).isTrue(); // untouched
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UserEntity buildUserWithRole(UserRoleEnum roleEnum) {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.addRole(roleEntityFor(roleEnum));
        return user;
    }

    private RoleEntity roleEntityFor(UserRoleEnum roleEnum) {
        RoleEntity role = new RoleEntity();
        role.setRole(roleEnum);
        return role;
    }
}
