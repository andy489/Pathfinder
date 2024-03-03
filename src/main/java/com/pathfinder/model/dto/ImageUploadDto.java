package com.pathfinder.model.dto;

import com.pathfinder.model.validation.route.CustomFile;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Accessors(chain = true)
public class ImageUploadDto {

    private Long routeId;

    @CustomFile(contentTypes = {"image/png", "image/jpeg", "image/jpg"})
    private MultipartFile picture;
}

