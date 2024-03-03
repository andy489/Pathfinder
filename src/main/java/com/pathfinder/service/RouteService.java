package com.pathfinder.service;

import com.pathfinder.exception.RouteNotFoundException;
import com.pathfinder.mapper.MapStructMapper;
import com.pathfinder.model.dto.MapCoordinatesDto;
import com.pathfinder.model.dto.RouteAddDto;
import com.pathfinder.model.entity.CategoryEntity;
import com.pathfinder.model.entity.CommentEntity;
import com.pathfinder.model.entity.PictureEntity;
import com.pathfinder.model.entity.RouteEntity;
import com.pathfinder.model.view.MostCommentedRouteView;
import com.pathfinder.model.view.RouteDetailsView;
import com.pathfinder.model.view.RouteIndexView;
import com.pathfinder.model.view.RouteWithCategoryView;
import com.pathfinder.repository.CommentRepository;
import com.pathfinder.repository.RouteRepository;
import io.jenetics.jpx.Bounds;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Metadata;
import io.jenetics.jpx.UInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteService.class);

    public static final String DEFAULT_PIC_URL = "/images/pic4.jpg";

    private static final String BASE_GPX_COORDINATES_PATH = "./src/main/resources/gpx/";

    private final RouteRepository routeRepository;

    private final MapStructMapper mapStructMapper;

    private final CategoryService categoryService;

    private final RouteRepository userRepository;
    private final CommentRepository commentRepository;


    @Autowired
    public RouteService(RouteRepository routeRepository, MapStructMapper mapStructMapper,
                        CategoryService categoryService, RouteRepository userRepository,
                        CommentRepository commentRepository) {

        this.routeRepository = routeRepository;
        this.mapStructMapper = mapStructMapper;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public MostCommentedRouteView getMostCommented() {

        Long mostCommentedRouteId = commentRepository.getMostCommentedRouteId();

        return MostCommentedRouteView.of(routeRepository.getReferenceById(mostCommentedRouteId));
    }

    public MostCommentedRouteView getRandFromMostCommented() {

        List<RouteEntity> allRoutes = routeRepository.findAll();

        long maxApprovedComments = allRoutes.stream()
                .max(Comparator.comparing(
                                route -> route.getComments().stream()
                                        .filter(CommentEntity::getApproved)
                                        .count()
                        )
                )
                .orElseThrow(NoSuchElementException::new)
                .getComments().stream().filter(CommentEntity::getApproved).count();

        List<MostCommentedRouteView> mostCommentedRouteViews = allRoutes.stream()
                .map(MostCommentedRouteView::of)
                .filter(r -> r.getApprovedComments().equals(maxApprovedComments))
                .toList();

        List<MostCommentedRouteView> mostCommented = new ArrayList<>(mostCommentedRouteViews);
        Collections.shuffle(mostCommented);

        return mostCommented.stream()
                .findFirst().orElseThrow(NoSuchElementException::new);
    }

    public void addRoute(RouteAddDto routeAddDto, Long authorId) {

        RouteEntity newRoute = mapStructMapper.toEntity(routeAddDto);
        newRoute.setCategories(categoryService.getCategoryEntities(routeAddDto.getCategories()));
        newRoute.setAuthor(userRepository.findById(authorId).orElseThrow(() -> new NoSuchElementException("Failed to find user."))
                .getAuthor());

        uploadGpxCoordinates(routeAddDto, newRoute);

        newRoute.setCategories(categoryService.getCategoryEntities(routeAddDto.getCategories()));

        routeRepository.saveAndFlush(newRoute);
    }

    public List<RouteIndexView> getAllRoutes() {

        return routeRepository.findAll().stream().map(
                r -> {
                    RouteIndexView route = mapStructMapper.toView(r);

                    route.setPictureUrl(
                            r.getPictures()
                                    .stream()
                                    .findAny()
                                    .orElse(new PictureEntity().setUrl(DEFAULT_PIC_URL))
                                    .getUrl());

                    return route;
                }
        ).toList();

    }

    public Optional<RouteEntity> getByVideoUrl(String videoUrl) {
        return routeRepository.findByVideoUrl(videoUrl);
    }

    public RouteDetailsView getRouteDetails(Long routeId) {
        RouteEntity routeEntity = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Failed to find route."));

        RouteDetailsView routeDetailsView = mapStructMapper.toDetailsView(routeEntity);

        routeDetailsView.setGpxCoordinates(new String(routeEntity.getGpxCoordinates().getBytes()));

        routeDetailsView.setPictureUrls(routeEntity.getPictures().stream()
                .map(PictureEntity::getUrl)
                .collect(Collectors.toSet())
        );

        routeDetailsView.setAuthorName(routeEntity.getAuthor().getFullName());

        return routeDetailsView;
    }

    public List<RouteWithCategoryView> getAllRoutesWithCategory(String type) {

        final String searchedType = type.toUpperCase();

        return routeRepository.findAll().stream().filter(
                r -> r.getCategories().stream().map(c -> c.getCategory().toString()).collect(Collectors.toSet())
                        .contains(searchedType)).map(r -> {
            RouteWithCategoryView route = mapStructMapper.toWithCategoryView(r);

            route.setPictureUrl(
                    r.getPictures().stream()
                            .findAny().orElse(new PictureEntity().setUrl(DEFAULT_PIC_URL))
                            .getUrl());


            return route.setCategoryTypes(
                    categoryService.getCategoryEntities(
                            r.getCategories().stream().map(CategoryEntity::getCategory)
                                    .collect(Collectors.toSet())
                    ).stream().map(c -> c.getCategory().toString()).collect(Collectors.toSet()));
        }).toList();
    }

    public Optional<RouteEntity> getById(Long routeId) {
        return routeRepository.findById(routeId);
    }

    private String getFilePath(String routeName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return String.format("%s_%s.xml",
                    convertRouteName(routeName),
                    UUID.randomUUID().toString().substring(0, 10));
        } else {
            throw new UsernameNotFoundException("User with username " + authentication.getName() + " not found");
        }
    }

    private String convertRouteName(String routeName) {

        return routeName.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("\"", "")
                .replaceAll("„", "")
                .replaceAll("“", "");
    }

    private void uploadGpxCoordinates(RouteAddDto routeAddDto, RouteEntity newRoute) {
        String filePathString = getFilePath(routeAddDto.getName());
        Path filePath = Paths.get(BASE_GPX_COORDINATES_PATH + filePathString);
        File file = new File(filePath.toUri());

        try {

            boolean mkdirs = file.getParentFile().mkdirs();
            boolean newFile = file.createNewFile();
            if (newFile) {
                LOGGER.warn("File {} created", file.getName());
            }

        } catch (IOException ioe) {
            LOGGER.warn("Failed to read gpx coordinates");
        }

        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(routeAddDto.getGpxCoordinates().getBytes());
            newRoute.setGpxCoordinates(filePathString);
        } catch (IOException ioe) {
            LOGGER.warn("Failed to write gpx coordinates from file {}", routeAddDto.getGpxCoordinates().getName());
        }
    }

    public MapCoordinatesDto getCoordinates(Long routeId) {

        RouteEntity routeEntity = this.routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Route with id=" + routeId + " not fount"));

        try {
            GPX gpx = GPX.read(Path.of(BASE_GPX_COORDINATES_PATH + routeEntity.getGpxCoordinates()));


            List<List<Double>> coordinates = gpx.getTracks().get(0).getSegments().get(0).getPoints().stream()
                    .map(p -> List.of(p.getLongitude().doubleValue(), p.getLatitude().doubleValue()))
                    .toList();

            Optional<UInt> number = gpx.getTracks().get(0).getNumber();

            double zoom = 12.5d;
            if (number.isPresent()) {
                zoom = number.get().doubleValue() / 10.0d;
            }

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
            throw new RuntimeException(e);
        }
    }
}
