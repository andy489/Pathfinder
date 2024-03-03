package com.pathfinder.web;

import com.pathfinder.model.dto.ImageUploadDto;
import com.pathfinder.model.user.PathfinderUserDetails;
import com.pathfinder.service.ImageCloudService;
import com.pathfinder.service.PictureService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping("/images")
public class ImageController extends GenericController {

    private final ImageCloudService imageCloudService;

    private final PictureService pictureService;

    @Autowired
    public ImageController(ImageCloudService imageCloudService, PictureService pictureService) {
        this.imageCloudService = imageCloudService;
        this.pictureService = pictureService;
    }

    @PostMapping("/upload")
    public ModelAndView uploadImage(@Valid ImageUploadDto imageUploadDto,
                                    @AuthenticationPrincipal PathfinderUserDetails principal) {

        Map<?, ?> map = imageCloudService.saveImage(imageUploadDto.getPicture());

        Object secureUrl = map.get("secure_url");
        Object publicId = map.get("public_id");

        String cloudPictureUrl = secureUrl.toString();
        String cloudPictureName = publicId.toString();

        pictureService.savePicture(cloudPictureUrl, cloudPictureName, principal.getId(), imageUploadDto.getRouteId());

        return super.redirect("/routes/details/" + imageUploadDto.getRouteId());
    }
}
