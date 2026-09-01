package com.pathfinder.service;

import com.pathfinder.exception.RouteNotFoundException;
import com.pathfinder.mapper.MapStructMapper;
import com.pathfinder.model.dto.RouteCommentsPartitionDto;
import com.pathfinder.model.dto.UserPermissionsDetailsDto;
import com.pathfinder.model.entity.CommentEntity;
import com.pathfinder.model.entity.RoleEntity;
import com.pathfinder.model.entity.RouteEntity;
import com.pathfinder.model.entity.UserEntity;
import com.pathfinder.model.enumerated.UserRoleEnum;
import com.pathfinder.model.view.AdminCommentView;
import com.pathfinder.model.view.AdminUsersView;
import com.pathfinder.model.view.CommentView;
import com.pathfinder.repository.CommentRepository;
import com.pathfinder.repository.RoleRepository;
import com.pathfinder.repository.RouteRepository;
import com.pathfinder.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

@Service
public class SuperuserService {

    private final RouteRepository routeRepository;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final CommentRepository commentRepository;

    private final MapStructMapper mapper;

    public SuperuserService(RouteRepository routeRepository, UserRepository userRepository,
                            RoleRepository roleRepository, CommentRepository commentRepository,
                            MapStructMapper mapper) {
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.commentRepository = commentRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public AdminCommentView getRouteComments() {
        List<RouteEntity> all = routeRepository.findAllWithComments();

        long approvedCount = 0;
        long newCount = 0;
        Map<String, RouteCommentsPartitionDto> routeComments = new TreeMap<>();

        for (RouteEntity r : all) {
            List<CommentView> approved = new java.util.ArrayList<>();
            List<CommentView> pending = new java.util.ArrayList<>();

            for (CommentEntity c : r.getComments()) {
                CommentView view = mapper.toView(c);
                if (Boolean.TRUE.equals(c.getApproved())) {
                    approved.add(view);
                    approvedCount++;
                } else {
                    pending.add(view);
                    newCount++;
                }
            }

            approved.sort(Comparator.comparing(CommentView::getCreated, Comparator.reverseOrder()));
            pending.sort(Comparator.comparing(CommentView::getCreated, Comparator.reverseOrder()));

            // keyed by id (String) so routes with identical names don't collide
            routeComments.put(String.valueOf(r.getId()), new RouteCommentsPartitionDto(r.getId(), approved, pending));
        }

        return new AdminCommentView()
                .setNewCommentsCount(newCount)
                .setApprovedCommentsCount(approvedCount)
                .setRouteComments(routeComments);
    }

    @Transactional
    public List<Long> deleteAllRouteComments(Long routeId, Boolean approved) {
        List<Long> ids = commentRepository.findIdsByRouteIdAndApproved(routeId, approved);
        commentRepository.deleteByRouteIdAndApproved(routeId, approved);
        return ids;
    }

    @Transactional
    public List<Long> approveRejAllRouteComments(Long routeId, Boolean approved) {
        RouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Route not found: " + routeId));

        List<Long> ids = new ArrayList<>();
        route.getComments().stream()
                .filter(c -> Objects.equals(c.getApproved(), approved))
                .forEach(c -> {
                    c.toggleApprove();
                    ids.add(c.getId());
                });

        return ids;
    }

    @Transactional(readOnly = true)
    public AdminUsersView getUserRoles() {
        List<UserEntity> all = userRepository.findAllWithRoles();

        List<UserPermissionsDetailsDto> admins = extractUsers(all, UserRoleEnum.ADMIN);
        List<UserPermissionsDetailsDto> moderators = extractUsers(all, UserRoleEnum.MODERATOR);
        List<UserPermissionsDetailsDto> users = extractUsers(all, UserRoleEnum.REGULAR);

        Map<String, List<UserPermissionsDetailsDto>> m = new TreeMap<>();
        m.put("admin", admins);
        m.put("moderator", moderators);
        m.put("regular", users);

        return new AdminUsersView()
                .setCount(all.size())
                .setUsersDetail(m);
    }

    private List<UserPermissionsDetailsDto> extractUsers(List<UserEntity> all, UserRoleEnum userRoleEnum) {
        return all.stream().filter(u -> {
            Set<RoleEntity> roles = u.getRoles();

            boolean isAdmin = false;
            boolean isModerator = false;

            for (RoleEntity role : roles) {
                UserRoleEnum roleEnum = role.getRole();
                if (roleEnum.equals(UserRoleEnum.ADMIN)) {
                    isAdmin = true;
                } else if (roleEnum.equals(UserRoleEnum.MODERATOR)) {
                    isModerator = true;
                }
            }

            return switch (userRoleEnum) {
                case ADMIN -> isAdmin;
                case MODERATOR -> isModerator && !isAdmin;
                case REGULAR -> !isModerator && !isAdmin;
            };
        }).map(mapper::toUserPermissionsDetailsDto).toList();
    }

    @Transactional
    public void togglePermUser(Long userId, UserRoleEnum from, UserRoleEnum to) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new java.util.NoSuchElementException("User not found: " + userId));

        if (user.containsRole(from)) {
            user.remove(from);
        }

        Optional<RoleEntity> roleEntityByRole = roleRepository.findRoleEntityByRole(to);
        roleEntityByRole.ifPresent(user::addRole);

        if (!user.containsRole(UserRoleEnum.REGULAR)) {
            roleEntityByRole = roleRepository.findRoleEntityByRole(UserRoleEnum.REGULAR);
            roleEntityByRole.ifPresent(user::addRole);
        }
    }
}
