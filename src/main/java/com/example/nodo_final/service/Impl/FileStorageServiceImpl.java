package com.example.nodo_final.service.Impl;

import com.example.nodo_final.entity.Resource;
import com.example.nodo_final.exception.StorageException;
import com.example.nodo_final.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.access-url-prefix:/api/files/}")
    private String accessUrlPrefix;

    private Path rootLocation;

    @Override
    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new StorageException("Cannot initialize storage directory: " + uploadDir);
        }
    }

    @Override
    public Resource save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty or null.");
        }

        String originalName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalName.trim().isEmpty()) {
            throw new StorageException("Original filename is invalid.");
        }
        if (originalName.contains("..")) {
            throw new StorageException("Filename contains invalid path sequence: " + originalName);
        }

        String extension = "";
        int idx = originalName.lastIndexOf('.');
        if (idx > 0 && idx < originalName.length() - 1) {
            extension = originalName.substring(idx);
        }

        String generatedUuid = UUID.randomUUID().toString();
        String newFileName = generatedUuid + extension;

        try {
            Path destinationFile = this.rootLocation.resolve(newFileName).normalize();
            if (!destinationFile.startsWith(this.rootLocation)) {
                throw new StorageException("Cannot store file outside the configured directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String accessUrl = StringUtils.trimTrailingCharacter(accessUrlPrefix, '/') + "/" + newFileName;

            return Resource.builder()
                    .resourceName(originalName)
                    .uuid(generatedUuid)
                    .url(accessUrl)
                    .build();

        } catch (IOException e) {
            throw new StorageException("Failed to store file " + originalName);
        }
    }

}
