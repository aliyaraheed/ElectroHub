package com.example.E_Commerce.service.impl;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadAndGetCloudinaryUrl(MultipartFile multipartFile) throws IOException {
        log.info("Uploading file to Cloudinary");

        // Convert MultipartFile to File
        File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);

        try {
            // Upload the file to Cloudinary
            Map result = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
            log.info("Cloudinary returned URL: {}", result.get("secure_url"));
            return (String) result.get("secure_url");
        } finally {
            // Delete the temporary file after upload
            if (tempFile.exists() && tempFile.delete()) {
                log.info("Temporary file deleted: {}", tempFile.getAbsolutePath());
            } else {
                log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
            }
        }
    }
}
