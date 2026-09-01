package com.pathfinder.service;

import com.pathfinder.exception.GpxProcessingException;
import com.pathfinder.exception.RouteNotFoundException;
import com.pathfinder.mapper.MapStructMapper;
import com.pathfinder.model.dto.MapCoordinatesDto;
import com.pathfinder.model.entity.PictureEntity;
import com.pathfinder.model.entity.RouteEntity;
import com.pathfinder.model.entity.UserEntity;
import com.pathfinder.model.view.RouteDetailsView;
import com.pathfinder.repository.RouteRepository;
import com.pathfinder.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock private RouteRepository routeRepository;
    @Mock private UserRepository userRepository;
    @Mock private MapStructMapper mapStructMapper;
    @Mock private CategoryService categoryService;
    @Mock private TranslationService translationService;

    @TempDir
    Path tempDir;

    private RouteService routeService;

    @BeforeEach
    void setUp() {
        routeService = new RouteService(
                routeRepository, userRepository, mapStructMapper,
                categoryService, translationService, tempDir.toString());
    }

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_returnsEmptyWhenNotFound() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(routeService.getById(99L)).isEmpty();
    }

    @Test
    void getById_returnsEntityWhenFound() {
        RouteEntity route = new RouteEntity();
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        assertThat(routeService.getById(1L)).contains(route);
    }

    // ── getRouteDetails ───────────────────────────────────────────────────────

    @Test
    void getRouteDetails_throwsRouteNotFoundWhenMissing() {
        when(routeRepository.findByIdWithPicturesAndAuthor(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> routeService.getRouteDetails(42L))
                .isInstanceOf(RouteNotFoundException.class);
    }

    @Test
    void getRouteDetails_setsAuthorNameUnknownWhenNoAuthor() throws IOException {
        RouteEntity route = new RouteEntity();
        route.setGpxCoordinates("no-such-file.xml");
        // no author set

        RouteDetailsView stub = new RouteDetailsView();
        when(routeRepository.findByIdWithPicturesAndAuthor(1L)).thenReturn(Optional.of(route));
        when(mapStructMapper.toDetailsView(route)).thenReturn(stub);

        RouteDetailsView result = routeService.getRouteDetails(1L);
        assertThat(result.getAuthorName()).isEqualTo("Unknown");
    }

    @Test
    void getRouteDetails_mapsPictureUrlsFromEntity() throws IOException {
        RouteEntity route = new RouteEntity();
        route.setGpxCoordinates("test.xml");
        UserEntity author = new UserEntity();
        author.setFullName("Alice");
        ReflectionTestUtils.setField(author, "id", 5L);
        route.setAuthor(author);

        PictureEntity pic = new PictureEntity().setUrl("https://example.com/pic.jpg");
        route.getPictures().add(pic);

        RouteDetailsView stub = new RouteDetailsView();
        when(routeRepository.findByIdWithPicturesAndAuthor(1L)).thenReturn(Optional.of(route));
        when(mapStructMapper.toDetailsView(route)).thenReturn(stub);

        RouteDetailsView result = routeService.getRouteDetails(1L);
        assertThat(result.getPictureUrls()).containsExactly("https://example.com/pic.jpg");
        assertThat(result.getAuthorName()).isEqualTo("Alice");
        assertThat(result.getAuthorId()).isEqualTo(5L);
    }

    // ── deleteRoute ───────────────────────────────────────────────────────────

    @Test
    void deleteRoute_throwsNotFoundForMissingRoute() {
        when(routeRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> routeService.deleteRoute(9L, 1L, false))
                .isInstanceOf(RouteNotFoundException.class);
    }

    @Test
    void deleteRoute_throwsAccessDeniedForNonOwner() {
        UserEntity owner = new UserEntity();
        ReflectionTestUtils.setField(owner, "id", 1L);

        RouteEntity route = new RouteEntity();
        route.setAuthor(owner);

        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> routeService.deleteRoute(10L, 2L, false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteRoute_succeeds_andDeletesGpxFile() throws IOException {
        Path gpxFile = Files.createFile(tempDir.resolve("route.xml"));

        UserEntity owner = new UserEntity();
        ReflectionTestUtils.setField(owner, "id", 1L);

        RouteEntity route = new RouteEntity();
        route.setAuthor(owner);
        route.setGpxCoordinates("route.xml");

        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));

        routeService.deleteRoute(10L, 1L, false);

        verify(routeRepository).delete(route);
        assertThat(Files.exists(gpxFile)).isFalse();
    }

    @Test
    void deleteRoute_adminCanDeleteOthersRoute() {
        UserEntity owner = new UserEntity();
        ReflectionTestUtils.setField(owner, "id", 1L);

        RouteEntity route = new RouteEntity();
        route.setAuthor(owner);
        route.setGpxCoordinates(null); // no gpx file

        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));

        routeService.deleteRoute(10L, 99L, true);

        verify(routeRepository).delete(route);
    }

    // ── getCoordinates ────────────────────────────────────────────────────────

    @Test
    void getCoordinates_throwsRouteNotFoundWhenMissing() {
        when(routeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> routeService.getCoordinates(1L))
                .isInstanceOf(RouteNotFoundException.class);
    }

    @Test
    void getCoordinates_throwsGpxProcessingExceptionForMissingFile() {
        RouteEntity route = new RouteEntity();
        route.setGpxCoordinates("nonexistent.xml");
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> routeService.getCoordinates(1L))
                .isInstanceOf(GpxProcessingException.class);
    }
}
