package com.path.pathfinder.mapper;

import com.path.pathfinder.model.dto.CommentCreationDto;
import com.path.pathfinder.model.dto.RouteAddDto;
import com.path.pathfinder.model.dto.UserPermissionsDetailsDto;
import com.path.pathfinder.model.dto.UserRegistrationDto;
import com.path.pathfinder.model.entity.CommentEntity;
import com.path.pathfinder.model.entity.RoleEntity;
import com.path.pathfinder.model.entity.RouteEntity;
import com.path.pathfinder.model.entity.UserEntity;
import com.path.pathfinder.model.user.PathfinderUserDetails;
import com.path.pathfinder.model.view.CommentView;
import com.path.pathfinder.model.view.RouteDetailsView;
import com.path.pathfinder.model.view.RouteIndexView;
import com.path.pathfinder.model.view.RouteWithCategoryView;
import com.path.pathfinder.model.view.UserProfileView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mapper(componentModel = "spring")
public interface MapStructMapper {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "level", ignore = true)
    UserEntity toEntity(UserRegistrationDto userRegistrationDto);

    UserProfileView toView(UserEntity userEntity);

    @Mapping(target = "pictureUrl", ignore = true)
    RouteIndexView toView(RouteEntity routeEntity);

    @Mapping(target = "gpxCoordinates", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "pictures", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "videoUrl", qualifiedByName = "fullUrlToYoutubeCode")
    RouteEntity toEntity(RouteAddDto routeAddDto);

    @Mapping(target = "gpxCoordinates", ignore = true)
    @Mapping(target = "pictureUrls", ignore = true)
    @Mapping(target = "authorName", ignore = true)
    RouteDetailsView toDetailsView(RouteEntity routeEntity);

    @Mapping(target = "pictureUrl", ignore = true)
    @Mapping(target = "categoryTypes", ignore = true)
    RouteWithCategoryView toWithCategoryView(RouteEntity routeEntity);

    @Mapping(target = "authorities", ignore = true)
    PathfinderUserDetails toUserDetails(UserEntity u);

    default GrantedAuthority toGrantedAuthority(RoleEntity roleEntity) {
        return new SimpleGrantedAuthority("ROLE_" + roleEntity.getRole().name());
    }

    @Mapping(target = "approved", constant = "false")
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "modified", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    CommentEntity toEntity(CommentCreationDto commentDto);

    @Mapping(target = "commentId", source = "id")
    @Mapping(target = "authorName", source = "author.fullName")
    CommentView toView(CommentEntity commentEntity);

    UserPermissionsDetailsDto toUserPermissionsDetailsDto(UserEntity userEntity);

    @Named("fullUrlToYoutubeCode")
    static String convertFullUrlToYoutubeCode(String fullUrl) {
        // https://regex101.com/

        String regex = "v=(.{11})";
        String regexShorts = "shorts.(.{11})";

        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(fullUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        compile = Pattern.compile(regexShorts);
        matcher = compile.matcher(fullUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
