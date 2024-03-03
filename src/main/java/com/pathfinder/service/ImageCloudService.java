package com.pathfinder.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pathfinder.config.CloudinaryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageCloudService {

    private final Cloudinary cloudinary;

    @Autowired
    public ImageCloudService(CloudinaryConfig cloudinaryConfig) {

        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryConfig.getCloudName(),
                "api_key", cloudinaryConfig.getApiKey(),
                "api_secret", cloudinaryConfig.getApiSecret(),
                "secure", true
        ));
    }

    public Map<?, ?> saveImage(MultipartFile multipartFile) {
        String imageId = UUID.randomUUID().toString();

        Map<?, ?> params = ObjectUtils.asMap(
                "public_id", imageId,
                "overwrite", true,
                "resource_type", "image"
        );

        File tmpFile = new File(imageId);

        Map<?, ?> upload;

        try {
            Files.write(tmpFile.toPath(), multipartFile.getBytes());
            copyFileUsingStream(multipartFile.getBytes(), tmpFile);

            upload = cloudinary.uploader().upload(tmpFile, params);

            Files.delete(tmpFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return upload;
    }

    private static void copyFileUsingStream(byte[] source, File dest) throws IOException {

        try (InputStream is = new ByteArrayInputStream(source);
             OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
}
