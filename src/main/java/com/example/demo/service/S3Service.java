package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface S3Service {
    void uploadFiles(List<MultipartFile> files, Long apartmentId) throws IOException;

    void deleteFile(Long apartmentId, String filename);

    List<String> listFiles(Long apartmentId);
}
