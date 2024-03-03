package com.path.pathfinder.service;

import com.path.pathfinder.model.entity.PictureEntity;
import com.path.pathfinder.model.entity.RouteEntity;
import com.path.pathfinder.model.entity.UserEntity;
import com.path.pathfinder.repository.PictureRepository;
import com.path.pathfinder.repository.RouteRepository;
import com.path.pathfinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PictureService {
    private static final Integer DISPLAYED_PICTURES_HOME = 12;

    private final PictureRepository pictureRepository;

    private final UserRepository userRepository;

    private final RouteRepository routeRepository;

    @Autowired
    public PictureService(PictureRepository pictureRepository,
                          UserRepository userRepository,
                          RouteRepository routeRepository) {

        this.pictureRepository = pictureRepository;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
    }

    public List<String> getSampleRoutePicturesUrls() {
        List<String> allUrls = pictureRepository.findAllUrls();

        Collections.shuffle(allUrls);

        allUrls = allUrls.subList(0, Math.min(DISPLAYED_PICTURES_HOME, allUrls.size()));

        return allUrls;
    }

    public void savePicture(String cloudUrl, String imgId, Long authorId, Long routeId) {

        UserEntity userEntity = userRepository.getReferenceById(authorId);
        RouteEntity routeEntity = routeRepository.getReferenceById(routeId);

        PictureEntity newPic = new PictureEntity()
                .setUrl(cloudUrl)
                .setTitle(imgId)
                .setAuthor(userEntity)
                .setRoute(routeEntity);

        pictureRepository.saveAndFlush(newPic);
    }
}
