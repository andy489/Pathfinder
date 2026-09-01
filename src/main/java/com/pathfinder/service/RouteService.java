package com.pathfinder.service;

import com.pathfinder.exception.GpxProcessingException;
import com.pathfinder.exception.RouteNotFoundException;
import com.pathfinder.mapper.MapStructMapper;
import com.pathfinder.model.dto.MapCoordinatesDto;
import com.pathfinder.model.dto.RouteAddDto;
import com.pathfinder.model.entity.PictureEntity;
import com.pathfinder.model.entity.RouteEntity;
import com.pathfinder.model.enumerated.RouteCategoryEnum;
import com.pathfinder.model.view.MostCommentedRouteView;
import com.pathfinder.model.view.RouteDetailsView;
import com.pathfinder.model.view.RouteIndexView;
import com.pathfinder.model.view.RouteWithCategoryView;
import com.pathfinder.repository.RouteRepository;
import com.pathfinder.repository.UserRepository;
import com.pathfinder.util.AppConstants;
import io.jenetics.jpx.Bounds;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteService.class);

    private static final int PAGE_SIZE = 6;

    private static final int TOP_COMMENTED_POOL = 5;

    public static final String DEFAULT_PIC_URL = AppConstants.DEFAULT_PIC_URL;

    private final String gpxStoragePath;

    private final RouteRepository routeRepository;

    private final UserRepository userRepository;

    private final MapStructMapper mapStructMapper;

    private final CategoryService categoryService;

    private final TranslationService translationService;

    public RouteService(RouteRepository routeRepository,
                        UserRepository userRepository,
                        MapStructMapper mapStructMapper,
                        CategoryService categoryService,
                        TranslationService translationService,
                        @Value("${pathfinder.gpx-storage-path}") String gpxStoragePath) {

        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.mapStructMapper = mapStructMapper;
        this.categoryService = categoryService;
        this.translationService = translationService;
        this.gpxStoragePath = gpxStoragePath;
    }

    @Transactional(readOnly = true)
    public MostCommentedRouteView getRandomFromTopCommented() {
        List<Long> topIds = routeRepository.findTopCommentedRouteIds(
                PageRequest.of(0, TOP_COMMENTED_POOL));
        if (topIds.isEmpty()) {
            return routeRepository.findFirstWithPictures()
                    .map(MostCommentedRouteView::of)
                    .orElseThrow(() -> new NoSuchElementException("No routes found"));
        }
        List<RouteEntity> topRoutes = routeRepository.findByIdsWithPictures(topIds);
        return MostCommentedRouteView.of(topRoutes.get(ThreadLocalRandom.current().nextInt(topRoutes.size())));
    }

    @Transactional
    public void addRoute(RouteAddDto routeAddDto, Long authorId, String username) {
        routeAddDto.setName(translationService.translateToBulgarian(routeAddDto.getName()));
        routeAddDto.setDescription(translationService.translateToBulgarian(routeAddDto.getDescription()));

        RouteEntity newRoute = mapStructMapper.toEntity(routeAddDto);
        newRoute.setCategories(categoryService.getCategoryEntities(routeAddDto.getCategories()));
        newRoute.setAuthor(userRepository.findById(authorId)
                .orElseThrow(() -> new NoSuchElementException("Failed to find user with id " + authorId)));

        uploadGpxCoordinates(routeAddDto, newRoute);

        routeRepository.saveAndFlush(newRoute);
    }

    @Transactional(readOnly = true)
    public List<RouteIndexView> getAllRoutes() {
        return routeRepository.findAllWithPictures().stream().map(
                r -> {
                    RouteIndexView route = mapStructMapper.toView(r);
                    route.setPictureUrl(resolveTileImage(r));
                    return route;
                }
        ).toList();
    }

    @Transactional(readOnly = true)
    public Page<RouteIndexView> getAllRoutesPaged(int page) {
        return routeRepository.findAllWithPicturesPaged(
                PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending())
        ).map(r -> {
            RouteIndexView route = mapStructMapper.toView(r);
            route.setPictureUrl(resolveTileImage(r));
            return route;
        });
    }

    @Transactional(readOnly = true)
    public Optional<RouteEntity> getByVideoUrl(String videoUrl) {
        return routeRepository.findByVideoUrl(videoUrl);
    }

    @Transactional(readOnly = true)
    public RouteDetailsView getRouteDetails(Long routeId) {
        // single query fetches route + pictures + author — avoids N+1
        RouteEntity routeEntity = routeRepository.findByIdWithPicturesAndAuthor(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Failed to find route."));

        RouteDetailsView routeDetailsView = mapStructMapper.toDetailsView(routeEntity);

        try {
            String gpxContent = Files.readString(
                    Paths.get(gpxStoragePath).resolve(routeEntity.getGpxCoordinates()));
            routeDetailsView.setGpxCoordinates(gpxContent);
        } catch (IOException e) {
            LOGGER.warn("Failed to read gpx file for route {}", routeId);
            routeDetailsView.setGpxCoordinates("");
        }

        routeDetailsView.setPictureUrls(routeEntity.getPictures().stream()
                .map(PictureEntity::getUrl)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new))
        );

        if (routeEntity.getAuthor() != null) {
            routeDetailsView.setAuthorName(routeEntity.getAuthor().getFullName());
            routeDetailsView.setAuthorId(routeEntity.getAuthor().getId());
        } else {
            routeDetailsView.setAuthorName("Unknown");
        }

        return routeDetailsView;
    }

    @Transactional(readOnly = true)
    public List<RouteWithCategoryView> getAllRoutesWithCategory(String type) {
        RouteCategoryEnum category;
        try {
            category = RouteCategoryEnum.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return List.of();
        }

        return routeRepository.findAllWithPicturesAndCategoriesByCategory(category).stream()
                .map(r -> {
                    RouteWithCategoryView route = mapStructMapper.toWithCategoryView(r);

                    route.setPictureUrl(
                            r.getPictures().stream()
                                    .findAny()
                                    .map(PictureEntity::getUrl)
                                    .orElse(AppConstants.DEFAULT_PIC_URL));

                    return route.setCategoryTypes(
                            r.getCategories().stream()
                                    .map(c -> c.getCategory().toString())
                                    .collect(Collectors.toSet()));
                }).toList();
    }

    @Transactional(readOnly = true)
    public Optional<RouteEntity> getById(Long routeId) {
        return routeRepository.findById(routeId);
    }

    @Transactional(readOnly = true)
    public List<RouteIndexView> searchRoutes(String query) {
        if (query == null || query.isBlank()) {
            return getAllRoutes();
        }
        return routeRepository.searchByNameOrDescription(query.trim()).stream()
                .map(r -> {
                    RouteIndexView route = mapStructMapper.toView(r);
                    route.setPictureUrl(resolveTileImage(r));
                    return route;
                }).toList();
    }

    private String resolveTileImage(RouteEntity r) {
        if (r.getMainPictureUrl() != null) {
            return r.getMainPictureUrl();
        }
        return r.getPictures().stream()
                .min(java.util.Comparator.comparingLong(PictureEntity::getId))
                .map(PictureEntity::getUrl)
                .orElse(DEFAULT_PIC_URL);
    }

    private String buildSafeFileName() {
        return UUID.randomUUID() + ".xml";
    }

    private void uploadGpxCoordinates(RouteAddDto routeAddDto, RouteEntity newRoute) {
        String fileName = buildSafeFileName();
        Path base = Paths.get(gpxStoragePath).normalize();
        Path filePath = base.resolve(fileName).normalize();

        if (!filePath.startsWith(base)) {
            throw new IllegalStateException("Potential path traversal detected");
        }

        try {
            Files.createDirectories(filePath.getParent());
            try (InputStream in = routeAddDto.getGpxCoordinates().getInputStream();
                 OutputStream out = Files.newOutputStream(filePath)) {
                in.transferTo(out);
            }
            newRoute.setGpxCoordinates(fileName);
        } catch (IOException ioe) {
            LOGGER.error("Failed to write GPX coordinates to {}", filePath, ioe);
            throw new GpxProcessingException("Failed to save GPX file for route", ioe);
        }
    }

    @Transactional(readOnly = true)
    public MapCoordinatesDto getCoordinates(Long routeId) {
        RouteEntity routeEntity = this.routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Route with id=" + routeId + " not found"));

        try {
            GPX gpx = GPX.read(Paths.get(gpxStoragePath).resolve(routeEntity.getGpxCoordinates()));

            if (gpx.getTracks().isEmpty() || gpx.getTracks().get(0).getSegments().isEmpty()) {
                throw new GpxProcessingException("GPX file has no tracks or segments for route " + routeId);
            }

            List<List<Double>> coordinates = gpx.getTracks().get(0).getSegments().get(0).getPoints().stream()
                    .map(p -> List.of(p.getLongitude().doubleValue(), p.getLatitude().doubleValue()))
                    .toList();

            double zoom = 12.5d;

            Optional<Bounds> bounds = gpx.getMetadata().flatMap(Metadata::getBounds);

            double maxLat = 0.0d;
            double maxLon = 0.0d;
            double minLat = 0.0d;
            double minLon = 0.0d;

            if (bounds.isPresent()) {
                Bounds b = bounds.get();
                maxLat = b.getMaxLatitude().doubleValue();
                maxLon = b.getMaxLongitude().doubleValue();
                minLat = b.getMinLatitude().doubleValue();
                minLon = b.getMinLongitude().doubleValue();
            }

            return new MapCoordinatesDto(coordinates, zoom, minLon, minLat, maxLon, maxLat);
        } catch (IOException e) {
            throw new GpxProcessingException("Failed to read GPX file for route " + routeId, e);
        }
    }

    @Transactional
    public void deleteRoute(Long routeId, Long requestingUserId, boolean isAdmin) {
        RouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Route not found: " + routeId));

        if (!isAdmin && !route.getAuthor().getId().equals(requestingUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not allowed to delete this route");
        }

        String gpxFile = route.getGpxCoordinates();
        routeRepository.delete(route);

        if (gpxFile != null) {
            try {
                Files.deleteIfExists(Paths.get(gpxStoragePath).resolve(gpxFile));
            } catch (IOException e) {
                LOGGER.warn("Could not delete GPX file {} for route {}", gpxFile, routeId, e);
            }
        }
    }
}
