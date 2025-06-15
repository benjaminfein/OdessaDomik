package com.example.demo.service.impl;

import com.example.demo.model.Apartment;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class S3ServiceImpl implements S3Service {
    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Autowired
    private ApartmentRepository apartmentRepository;

    private S3Client createS3Client() {
        log.debug("[S3ServiceImpl] Создание S3-клиента с регионом: {}, bucket: {}", region, bucketName);
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Override
    public void uploadFiles(List<MultipartFile> files, Long apartmentId) throws IOException {
        log.info("[S3ServiceImpl] Загрузка {} файлов в S3 для квартиры с ID {}", files.size(), apartmentId);
        S3Client s3Client = createS3Client();
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> {
                    log.error("[S3ServiceImpl] Квартира с ID {} не найдена при попытке загрузки файлов", apartmentId);
                    return new RuntimeException("Квартира не найдена");
                });

        for (MultipartFile file : files) {
            String key = "apartments/" + apartmentId + "/" + file.getOriginalFilename();
            log.debug("[S3ServiceImpl] Загрузка файла: {}, ключ: {}", file.getOriginalFilename(), key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
            apartment.getPhotoUrls().add(fileUrl);
            log.debug("[S3ServiceImpl] Файл загружен: {}", fileUrl);
        }

        apartmentRepository.save(apartment);
        log.info("[S3ServiceImpl] Файлы успешно загружены и сохранены для квартиры {}", apartmentId);
    }

    @Override
    public void deleteFiles(Long apartmentId, List<String> fileNames) {
        log.info("[S3ServiceImpl] Удаление {} файлов из S3 для квартиры с ID {}", fileNames.size(), apartmentId);
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> {
                    log.error("[S3ServiceImpl] Квартира с ID {} не найдена при попытке удаления файлов", apartmentId);
                    return new RuntimeException("Квартира не найдена");
                });

        List<String> updatedUrls = new ArrayList<>(apartment.getPhotoUrls());

        for (String fileName : fileNames) {
            String key = "apartments/" + apartmentId + "/" + fileName;
            String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;

            log.debug("[S3ServiceImpl] Удаление файла: {}", fileUrl);

            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key));

            updatedUrls.removeIf(url -> url.equals(fileUrl));
        }

        apartment.setPhotoUrls(updatedUrls);
        apartmentRepository.save(apartment);
        log.info("[S3ServiceImpl] Файлы успешно удалены и изменения сохранены для квартиры {}", apartmentId);
    }

    @Override
    public List<String> listFiles(Long apartmentId) {
        log.info("[S3ServiceImpl] Получение списка файлов из S3 для квартиры с ID {}", apartmentId);
        S3Client s3Client = createS3Client();

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("apartments/" + apartmentId + "/")
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<String> fileUrls = response.contents().stream()
                .map(s3Object -> String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Object.key()))
                .collect(Collectors.toList());

        log.debug("[S3ServiceImpl] Найдено {} файлов для квартиры {}", fileUrls.size(), apartmentId);
        return fileUrls;
    }
}
