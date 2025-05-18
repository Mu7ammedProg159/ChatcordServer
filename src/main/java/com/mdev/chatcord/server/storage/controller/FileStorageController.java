package com.mdev.chatcord.server.storage.controller;

import com.mdev.chatcord.server.storage.service.FileStorageService;
import com.mdev.chatcord.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@EnableMethodSecurity
@RequestMapping("/api/users/{uuid}/files")
public class FileStorageController {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER') and #uuid == authentication.token.claims['uuid']")
    public ResponseEntity<?> uploadFile(@PathVariable String uuid, @RequestParam("file") MultipartFile file){
        String fileName = fileStorageService.storeFile(file, uuid);
        return ResponseEntity.ok(Map.of("message", "File uploaded", "filename", fileName));
    }

    @GetMapping("/download/{filename}")
    @PreAuthorize("hasRole('USER') and #uuid == authentication.token.claims['uuid']")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String uuid,
            @PathVariable String filename
    ) {
        Resource file = fileStorageService.loadFileAsResource(uuid, filename);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
