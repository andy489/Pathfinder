package com.path.pathfinder.service;

import com.path.pathfinder.mapper.MapStructMapper;
import com.path.pathfinder.model.dto.RouteCommentDto;
import com.path.pathfinder.model.dto.RouteCommentsPartitionDto;
import com.path.pathfinder.model.dto.UserPermissionsDetailsDto;
import com.path.pathfinder.model.entity.CommentEntity;
import com.path.pathfinder.model.entity.GenericEntity;
import com.path.pathfinder.model.entity.RoleEntity;
import com.path.pathfinder.model.entity.RouteEntity;
import com.path.pathfinder.model.entity.UserEntity;
import com.path.pathfinder.model.enumerated.UserRoleEnum;
import com.path.pathfinder.model.view.AdminCommentView;
import com.path.pathfinder.model.view.AdminUsersView;
import com.path.pathfinder.model.view.CommentView;
import com.path.pathfinder.repository.RoleRepository;
import com.path.pathfinder.repository.RouteRepository;
import com.path.pathfinder.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import static java.util.stream.Collectors.toMap;

@Service
public class SuperuserService {

    private final RouteRepository routeRepository;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final MapStructMapper mapper;

    public SuperuserService(RouteRepository routeRepository, UserRepository userRepository,
                            RoleRepository roleRepository, MapStructMapper mapper) {

        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
    }

    @Transactional
    public AdminCommentView getRouteComments() {

        List<RouteEntity> all = routeRepository.findAll();

        Map<String, RouteCommentsPartitionDto> collect = all.stream()
                .map(r -> {
                    Long routeId = r.getId();
                    String routeName = r.getName();

                    List<CommentView> newComments = r.getComments().stream().filter(c -> !c.getApproved())
                            .map(mapper::toView)
                            .sorted(Comparator.comparing(CommentView::getCreated, Comparator.reverseOrder()))
                            .toList();

                    List<CommentView> approvedComments = r.getComments().stream().filter(CommentEntity::getApproved)
                            .map(mapper::toView)
                            .sorted(Comparator.comparing(CommentView::getCreated, Comparator.reverseOrder()))
                            .toList();

                    RouteCommentsPartitionDto routeCommentsPartitionDto =
                            new RouteCommentsPartitionDto(routeId, approvedComments, newComments);

                    return new RouteCommentDto(routeName, routeCommentsPartitionDto);
                }).collect(toMap(RouteCommentDto::routeName, RouteCommentDto::routeCommentsPartitionDto));


        long approvedCommentsCount = all.stream()
                .flatMap(r -> r.getComments().stream())
                .filter(CommentEntity::getApproved)
                .count();

        long newCommentsCount = all.stream()
                .flatMap(r -> r.getComments().stream())
                .filter(c -> !c.getApproved())
                .count();


        return new AdminCommentView().setNewCommentsCount(newCommentsCount)
                .setApprovedCommentsCount(approvedCommentsCount)
                .setRouteComments(collect);
    }

    @Transactional
    public List<Long> deleteAllRouteComments(Long routeId, Boolean approved) {

        RouteEntity referenceById = routeRepository.getReferenceById(routeId);

        List<CommentEntity> toRemove = referenceById.getComments().stream()
                .filter(c -> c.getApproved().equals(approved))
                .toList();

        toRemove.forEach(referenceById.getComments()::remove);

        return toRemove.stream().map(GenericEntity::getId).toList();
    }

    @Transactional
    public List<Long> approveRejAllRouteComments(Long routeId, Boolean approved) {

        RouteEntity referenceById = routeRepository.getReferenceById(routeId);

        List<Long> toToggleIds = referenceById.getComments().stream()
                .filter(c -> c.getApproved().equals(approved))
                .map(CommentEntity::getId)
                .toList();

        referenceById.getComments().stream()
                .filter(c -> c.getApproved().equals(approved))
                .forEach(CommentEntity::toggleApprove);

        return toToggleIds;
    }

    public AdminUsersView getUserRoles() {

        List<UserEntity> all = userRepository.findAll();

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
                case ADMIN -> filterAdmin(isModerator, isAdmin);
                case MODERATOR -> filterModerator(isModerator, isAdmin);
                case REGULAR -> filterUser(isModerator, isAdmin);
            };

        }).map(mapper::toUserPermissionsDetailsDto).toList();
    }

    private boolean filterAdmin(boolean isModerator, boolean isAdmin) {
        return isAdmin;
    }

    private boolean filterModerator(boolean isModerator, boolean isAdmin) {
        return isModerator && !isAdmin;
    }

    private boolean filterUser(boolean isModerator, boolean isAdmin) {
        return !isModerator && !isAdmin;
    }

    @Transactional
    public void togglePermUser(Long userId, UserRoleEnum from, UserRoleEnum to) {

        UserEntity referenceById = userRepository.getReferenceById(userId);


        if (referenceById.containsRole(from)) {
            referenceById.remove(from);
        }

        Optional<RoleEntity> roleEntityByRole = roleRepository.findRoleEntityByRole(to);
        roleEntityByRole.ifPresent(referenceById::addRole);

        if (!referenceById.containsRole(UserRoleEnum.REGULAR)) {
            roleEntityByRole = roleRepository.findRoleEntityByRole(UserRoleEnum.REGULAR);
            roleEntityByRole.ifPresent(referenceById::addRole);
        }
    }
}
