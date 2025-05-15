package com.zuzu.reviewservice.infrastructure.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    private S3ServiceImpl s3Service;
    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        s3Service = new S3ServiceImpl(s3Client);
        ReflectionTestUtils.setField(s3Service, "bucketName", bucketName);
    }

    @Test
    void shouldListFilesSuccessfully() {
        // Arrange
        String prefix = "reviews/";
        
        S3Object object1 = S3Object.builder().key("reviews/file1.jl").build();
        S3Object object2 = S3Object.builder().key("reviews/file2.jl").build();
        
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(object1, object2)
                .build();
        
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        // Act
        List<String> result = s3Service.listFiles(prefix);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("reviews/file1.jl", "reviews/file2.jl")));
    }

    @Test
    void shouldDownloadFileSuccessfully() {
        // Arrange
        String fileKey = "reviews/file1.jl";
        String content = "test content";
        InputStream contentStream = new ByteArrayInputStream(content.getBytes());
        
        GetObjectResponse getObjectResponse = GetObjectResponse.builder().build();
        ResponseInputStream<GetObjectResponse> responseStream = 
                new ResponseInputStream<>(getObjectResponse, AbortableInputStream.create(contentStream));
        
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

        // Act
        InputStream result = s3Service.downloadFile(fileKey);

        // Assert
        assertNotNull(result);
    }

    @Test
    void shouldCheckIfFileExists() {
        // Arrange
        String fileKey = "reviews/file1.jl";
        
        HeadObjectResponse headObjectResponse = HeadObjectResponse.builder().build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);

        // Act
        boolean result = s3Service.fileExists(fileKey);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenFileDoesNotExist() {
        // Arrange
        String fileKey = "reviews/non-existent.jl";
        
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        // Act
        boolean result = s3Service.fileExists(fileKey);

        // Assert
        assertFalse(result);
    }
} 