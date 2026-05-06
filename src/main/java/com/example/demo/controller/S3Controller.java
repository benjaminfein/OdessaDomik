package com.example.demo.controller;

import com.example.demo.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {
    private final S3Service s3Service;

    @PostMapping("/upload-multiple")
    public ResponseEntity<String> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("apartmentId") Long apartmentId) {
        try {
            s3Service.uploadFiles(files, apartmentId);
            return ResponseEntity.ok("Files uploaded and attached to apartment with id \"" + apartmentId + "\" successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error while file uploading: " + e.getMessage());
        }
    }

    @GetMapping("/list/{apartmentId}")
    public ResponseEntity<List<String>> listFiles(@PathVariable Long apartmentId) {
        return ResponseEntity.ok(s3Service.listFiles(apartmentId));
    }

    @DeleteMapping("/delete-multiple/{apartmentId}")
    public ResponseEntity<String> deleteFiles(@PathVariable Long apartmentId,
                                              @RequestParam("fileNames") List<String> fileNames) {
        s3Service.deleteFiles(apartmentId, fileNames);
        return ResponseEntity.ok("Files deleted successfully.");
    }
}