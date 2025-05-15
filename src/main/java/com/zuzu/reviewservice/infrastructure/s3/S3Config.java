package com.zuzu.reviewservice.infrastructure.s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${aws.endpoint:http://review-service-localstack:4566}")
    private String endpoint;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.accessKey:test}")
    private String accessKey;

    @Value("${aws.secretKey:test}")
    private String secretKey;
    @Bean(name = "customS3Client")
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .forcePathStyle(true)
                .build();
    }
}
