package com.mdev.chatcord.server.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@NoArgsConstructor(force = true)
public class FileStorageService {

    @Value("${file.upload-dir}")
    private final Path uploadDir;
    @Value("${file.download-dir}")
    private final Path downloadDir;

    public FileStorageService(
            String uploadDir,
            String downloadDir
    ) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.downloadDir = Paths.get(downloadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadDir);
            Files.createDirectories(this.downloadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directories!", e);
        }
    }

    public String storeFile(MultipartFile file, String uuid) {
        try {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            Path userFolder = uploadDir.resolve(uuid);
            Files.createDirectories(userFolder);

            Path targetPath = userFolder.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public Resource loadFileAsResource(String uuid, String filename) {
        try {
            Path filePath = uploadDir.resolve(uuid).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            }
            throw new FileNotFoundException("File not found: " + filename);
        } catch (MalformedURLException | FileNotFoundException e) {
            throw new RuntimeException("File loading failed", e);
        }
    }

}
