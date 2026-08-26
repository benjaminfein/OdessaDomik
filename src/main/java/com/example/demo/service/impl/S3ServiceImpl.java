package com.example.demo.service.impl;

import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.model.Apartment;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final ApartmentRepository apartmentRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    @Override
    public void uploadFiles(List<MultipartFile> files, Long apartmentId) throws IOException {
        log.info("[S3ServiceImpl] Uploading {} file(s) to S3 for apartment id={}", files.size(), apartmentId);
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> {
                    log.error("[S3ServiceImpl] Apartment id={} not found during file upload", apartmentId);
                    return new ApartmentNotFoundException("Apartment not found");
                });

        for (MultipartFile file : files) {
            String key = "apartments/" + apartmentId + "/" + file.getOriginalFilename();
            log.debug("[S3ServiceImpl] Uploading file: {}, key: {}", file.getOriginalFilename(), key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
            apartment.getPhotoUrls().add(fileUrl);
            log.debug("[S3ServiceImpl] Uploaded: {}", fileUrl);
        }

        apartmentRepository.save(apartment);
        log.info("[S3ServiceImpl] All files uploaded and saved for apartment id={}", apartmentId);
    }

    @Override
    public void deleteFiles(Long apartmentId, List<String> fileNames) {
        log.info("[S3ServiceImpl] Deleting {} file(s) from S3 for apartment id={}", fileNames.size(), apartmentId);
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> {
                    log.error("[S3ServiceImpl] Apartment id={} not found during file deletion", apartmentId);
                    return new ApartmentNotFoundException("Apartment not found");
                });

        List<String> updatedUrls = new ArrayList<>(apartment.getPhotoUrls());

        for (String fileName : fileNames) {
            String key = "apartments/" + apartmentId + "/" + fileName;
            String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;

            log.debug("[S3ServiceImpl] Deleting file: {}", fileUrl);
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key));
            updatedUrls.removeIf(url -> url.equals(fileUrl));
        }

        apartment.setPhotoUrls(updatedUrls);
        apartmentRepository.save(apartment);
        log.info("[S3ServiceImpl] Files deleted and changes saved for apartment id={}", apartmentId);
    }

    @Override
    public List<String> listFiles(Long apartmentId) {
        log.info("[S3ServiceImpl] Listing files from S3 for apartment id={}", apartmentId);

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("apartments/" + apartmentId + "/")
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<String> fileUrls = response.contents().stream()
                .map(s3Object -> String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Object.key()))
                .collect(Collectors.toList());

        log.debug("[S3ServiceImpl] Found {} file(s) for apartment id={}", fileUrls.size(), apartmentId);
        return fileUrls;
    }
}
