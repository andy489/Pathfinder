package com.pathfinder.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pathfinder.config.CloudinaryConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageCloudService {

    private final Cloudinary cloudinary;

    public ImageCloudService(CloudinaryConfig cloudinaryConfig) {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryConfig.getCloudName(),
                "api_key", cloudinaryConfig.getApiKey(),
                "api_secret", cloudinaryConfig.getApiSecret(),
                "secure", true
        ));
    }

    public Map<?, ?> saveImage(MultipartFile multipartFile) {
        Path tmpFile = null;
        try {
            String imageId = UUID.randomUUID().toString();
            Map<?, ?> params = ObjectUtils.asMap(
                    "public_id", imageId,
                    "overwrite", true,
                    "resource_type", "image"
            );

            tmpFile = Files.createTempFile(imageId, ".tmp");
            multipartFile.transferTo(tmpFile);

            return cloudinary.uploader().upload(tmpFile.toFile(), params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (tmpFile != null) {
                try {
                    Files.deleteIfExists(tmpFile);
                } catch (IOException ignored) {
                }
            }
        }
    }
}
