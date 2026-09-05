package com.pathfinder.web;

import com.pathfinder.model.dto.ImageUploadDto;
import com.pathfinder.model.user.PathfinderUserDetails;
import com.pathfinder.service.ImageCloudService;
import com.pathfinder.service.PictureService;
import com.pathfinder.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/images")
public class ImageController extends GenericController {

    private final ImageCloudService imageCloudService;

    private final PictureService pictureService;

    private final RouteService routeService;

    public ImageController(ImageCloudService imageCloudService, PictureService pictureService, RouteService routeService) {
        this.imageCloudService = imageCloudService;
        this.pictureService = pictureService;
        this.routeService = routeService;
    }

    @PostMapping("/upload")
    public ModelAndView uploadImage(@Valid ImageUploadDto imageUploadDto,
                                    @AuthenticationPrincipal PathfinderUserDetails principal) {

        var route = routeService.getById(imageUploadDto.getRouteId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Route not found"));

        if (route.getAuthor() == null || !route.getAuthor().getId().equals(principal.getId())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "Only the route author can upload pictures");
        }

        Map<?, ?> map = imageCloudService.saveImage(imageUploadDto.getPicture());

        Object secureUrl = map.get("secure_url");
        Object publicId = map.get("public_id");

        String cloudPictureUrl = secureUrl.toString();
        String cloudPictureName = publicId.toString();

        pictureService.savePicture(cloudPictureUrl, cloudPictureName, principal.getId(), imageUploadDto.getRouteId());

        return super.redirect("/routes/details/" + imageUploadDto.getRouteId());
    }
}
