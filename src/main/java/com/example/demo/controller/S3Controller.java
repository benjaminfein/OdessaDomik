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
            return ResponseEntity.ok("Файлы успешно загружены и привязаны к квартире.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при загрузке файлов: " + e.getMessage());
        }
    }

    @GetMapping("/list/{apartmentId}")
    public ResponseEntity<List<String>> listFiles(@PathVariable Long apartmentId) {
        return ResponseEntity.ok(s3Service.listFiles(apartmentId));
    }

    @DeleteMapping("/delete/{apartmentId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long apartmentId,
                                             @RequestParam("fileName") String fileName) {
        s3Service.deleteFile(apartmentId, fileName);
        return ResponseEntity.ok("Файл удалён.");
    }
}