package com.pathfinder.service;

import com.pathfinder.model.entity.PictureEntity;
import com.pathfinder.model.entity.RouteEntity;
import com.pathfinder.model.entity.UserEntity;
import com.pathfinder.repository.PictureRepository;
import com.pathfinder.repository.RouteRepository;
import com.pathfinder.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PictureService {

    private static final int DISPLAYED_PICTURES_HOME = 12;

    private final PictureRepository pictureRepository;

    private final UserRepository userRepository;

    private final RouteRepository routeRepository;

    public PictureService(PictureRepository pictureRepository,
                          UserRepository userRepository,
                          RouteRepository routeRepository) {
        this.pictureRepository = pictureRepository;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
    }

    @Transactional(readOnly = true)
    public List<String> getSampleRoutePicturesUrls() {
        return pictureRepository.findRandomUrls(DISPLAYED_PICTURES_HOME);
    }

    @Transactional
    public void savePicture(String cloudUrl, String imgId, Long authorId, Long routeId) {
        UserEntity userEntity = userRepository.findById(authorId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + authorId));
        RouteEntity routeEntity = routeRepository.findById(routeId)
                .orElseThrow(() -> new NoSuchElementException("Route not found: " + routeId));

        PictureEntity newPic = new PictureEntity()
                .setUrl(cloudUrl)
                .setTitle(imgId)
                .setAuthor(userEntity)
                .setRoute(routeEntity);

        pictureRepository.saveAndFlush(newPic);
    }
}
