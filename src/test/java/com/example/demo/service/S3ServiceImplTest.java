package com.example.demo.service;

import com.example.demo.model.Apartment;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.service.impl.S3ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceImplTest {

    @Mock private S3Client s3Client;
    @Mock private ApartmentRepository apartmentRepository;

    @InjectMocks
    private S3ServiceImpl s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "region", "eu-central-1");
    }

    @Test
    void uploadFiles_ShouldAddUrlsToApartmentAndSave() throws IOException {
        Apartment apartment = new Apartment(1L, "Apt", null, null, "addr", 1000,
                false, false, 1, 50, false, 2, new ArrayList<>());

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        s3Service.uploadFiles(List.of(file), 1L);

        assertEquals(1, apartment.getPhotoUrls().size());
        assertTrue(apartment.getPhotoUrls().get(0).contains("photo.jpg"));
        verify(apartmentRepository).save(apartment);
    }

    @Test
    void uploadFiles_ShouldThrow_WhenApartmentNotFound() {
        when(apartmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                s3Service.uploadFiles(List.of(mock(MultipartFile.class)), 99L));
    }

    @Test
    void deleteFiles_ShouldRemoveUrlsAndSave() {
        List<String> photoUrls = new ArrayList<>();
        photoUrls.add("https://test-bucket.s3.eu-central-1.amazonaws.com/apartments/1/photo.jpg");

        Apartment apartment = new Apartment(1L, "Apt", null, null, "addr", 1000,
                false, false, 1, 50, false, 2, photoUrls);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));

        s3Service.deleteFiles(1L, List.of("photo.jpg"));

        assertTrue(apartment.getPhotoUrls().isEmpty());
        verify(apartmentRepository).save(apartment);
    }

    @Test
    void deleteFiles_ShouldThrow_WhenApartmentNotFound() {
        when(apartmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> s3Service.deleteFiles(99L, List.of("photo.jpg")));
    }

    @Test
    void listFiles_ShouldReturnFileUrls() {
        S3Object obj = S3Object.builder().key("apartments/1/photo.jpg").build();
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(List.of(obj))
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<String> result = s3Service.listFiles(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).contains("photo.jpg"));
    }

    @Test
    void listFiles_ShouldReturnEmpty_WhenNoFiles() {
        ListObjectsV2Response response = ListObjectsV2Response.builder().contents(List.of()).build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<String> result = s3Service.listFiles(1L);

        assertTrue(result.isEmpty());
    }
}
