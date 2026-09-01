package com.pathfinder.web;

import com.pathfinder.model.dto.ImageUploadDto;
import com.pathfinder.model.entity.RouteEntity;
import com.pathfinder.model.entity.UserEntity;
import com.pathfinder.model.user.PathfinderUserDetails;
import com.pathfinder.service.ImageCloudService;
import com.pathfinder.service.PictureService;
import com.pathfinder.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock private ImageCloudService imageCloudService;
    @Mock private PictureService pictureService;
    @Mock private RouteService routeService;

    private ImageController controller;
    private PathfinderUserDetails principal;

    @BeforeEach
    void setUp() {
        controller = new ImageController(imageCloudService, pictureService, routeService);

        UserEntity author = new UserEntity();
        ReflectionTestUtils.setField(author, "id", 1L);

        principal = new PathfinderUserDetails();
        principal.setId(1L);
    }

    private ImageUploadDto dto(Long routeId) {
        ImageUploadDto dto = new ImageUploadDto();
        dto.setRouteId(routeId);
        dto.setPicture(new MockMultipartFile("pic", "pic.jpg", "image/jpeg", new byte[]{1, 2, 3}));
        return dto;
    }

    @Test
    void uploadImage_routeNotFound_throws404() {
        when(routeService.getById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.uploadImage(dto(99L), principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Route not found");
    }

    @Test
    void uploadImage_notAuthor_throws403() {
        UserEntity otherAuthor = new UserEntity();
        ReflectionTestUtils.setField(otherAuthor, "id", 2L);

        RouteEntity route = new RouteEntity();
        route.setAuthor(otherAuthor);

        when(routeService.getById(1L)).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> controller.uploadImage(dto(1L), principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("author");
    }

    @Test
    void uploadImage_cloudinaryReturnsNullSecureUrl_throws502() {
        UserEntity author = new UserEntity();
        ReflectionTestUtils.setField(author, "id", 1L);

        RouteEntity route = new RouteEntity();
        route.setAuthor(author);

        when(routeService.getById(1L)).thenReturn(Optional.of(route));
        when(imageCloudService.saveImage(any())).thenReturn((Map) Map.of("public_id", "abc"));

        assertThatThrownBy(() -> controller.uploadImage(dto(1L), principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("incomplete response");
    }

    @Test
    void uploadImage_success_savesPictureAndRedirects() {
        UserEntity author = new UserEntity();
        ReflectionTestUtils.setField(author, "id", 1L);

        RouteEntity route = new RouteEntity();
        route.setAuthor(author);

        when(routeService.getById(1L)).thenReturn(Optional.of(route));
        when(imageCloudService.saveImage(any())).thenReturn(
                (Map) Map.of("secure_url", "https://cdn.example.com/img.jpg", "public_id", "folder/img"));

        ModelAndView mav = controller.uploadImage(dto(1L), principal);

        verify(pictureService).savePicture("https://cdn.example.com/img.jpg", "folder/img", 1L, 1L);
        assertThat(mav.getViewName()).contains("redirect");
    }
}
