package com.zuzu.reviewservice.infrastructure.s3;

import com.zuzu.reviewservice.application.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /*@Autowired
    public S3ServiceImpl(@Qualifier("customS3Client") S3Client s3Client) {
        this.s3Client = s3Client;
    }*/

    @Override
    public List<String> listFiles(String prefix) {
        try {
           ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
            return response.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
        } catch (S3Exception e) {
            log.error("Error listing files from S3 with prefix {}: {}", prefix, e.getMessage(), e);
            throw new RuntimeException("Error listing files from S3", e);
        }
    }

    @Override
    public InputStream downloadFile(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            return response;
        } catch (S3Exception e) {
            log.error("Error downloading file {} from S3: {}", key, e.getMessage(), e);
            throw new RuntimeException("Error downloading file from S3", e);
        }
    }

    @Override
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Error checking if file {} exists in S3: {}", key, e.getMessage(), e);
            throw new RuntimeException("Error checking if file exists in S3", e);
        }
    }
}