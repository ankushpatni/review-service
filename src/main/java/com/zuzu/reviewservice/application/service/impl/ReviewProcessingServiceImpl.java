package com.zuzu.reviewservice.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuzu.reviewservice.application.dto.ReviewDto;
import com.zuzu.reviewservice.application.service.ProcessedFileService;
import com.zuzu.reviewservice.application.service.ReviewProcessingService;
import com.zuzu.reviewservice.application.service.S3Service;
import com.zuzu.reviewservice.domain.model.ProcessedFile;
import com.zuzu.reviewservice.infrastructure.kafka.ReviewEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewProcessingServiceImpl implements ReviewProcessingService {

    private final S3Service s3Service;
    private final ProcessedFileService processedFileService;
    private final ObjectMapper objectMapper;
    private final ReviewEventProducer reviewEventProducer;

    @Value("${aws.s3.prefix}")
    private String s3Prefix;

    @Value("${review-service.processing.processed-files-tracking-enabled:true}")
    private boolean processedFilesTrackingEnabled;

    @Override
    public void processNewReviewFiles() {
        try {
            log.info("Starting to process new review files from S3..." + s3Prefix);
            // 1. List all files in the S3 bucket
            List<String> allFiles = s3Service.listFiles(s3Prefix);
            log.info("Found {} files in S3 bucket with prefix: {}", allFiles.size(), s3Prefix);
            
            // 2. Find files that haven't been processed yet
            List<String> newFiles = findNewFiles(allFiles);
            log.info("Found {} new files to process", newFiles.size());
            
            // 3. Process each new file asynchronously
            List<CompletableFuture<ProcessedFile>> futures = new ArrayList<>();
            
            for (String fileName : newFiles) {
                try {
                    InputStream fileContent = s3Service.downloadFile(fileName);
                    CompletableFuture<ProcessedFile> future = processFile(fileName, fileContent);
                    futures.add(future);
                } catch (Exception e) {
                    log.error("Error downloading file {}: {}", fileName, e.getMessage(), e);
                }
            }
            
            // 4. Wait for all tasks to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            log.info("Completed processing {} files", newFiles.size());
        } catch (Exception e) {
            log.error("Error processing review files: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<ProcessedFile> processFile(String fileName, InputStream fileContent) {
        log.info("Processing file: {}", fileName);
        int recordsProcessed = 0;
        int recordsFailed = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileContent))) {
            String line;
            List<CompletableFuture<?>> publishFutures = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    // Parse the review from the file
                    ReviewDto reviewDto = objectMapper.readValue(line, ReviewDto.class);

                    // Publish to Kafka instead of directly processing
                    CompletableFuture<?> future = reviewEventProducer.publishReviewEvent(reviewDto);
                    publishFutures.add(future);
                    
                    recordsProcessed++;
                } catch (Exception e) {
                    log.error("Error publishing review from file {}: {}", fileName, e.getMessage());
                    recordsFailed++;
                }
            }
            
            // Wait for all publish operations to complete
            CompletableFuture.allOf(publishFutures.toArray(new CompletableFuture[0])).join();
            
            ProcessedFile.ProcessingStatus status = determineStatus(recordsProcessed, recordsFailed);
            
            // Only mark file as processed if tracking is enabled
            if (processedFilesTrackingEnabled) {
                return CompletableFuture.completedFuture(
                        processedFileService.markFileAsProcessed(fileName, status, recordsProcessed, recordsFailed)
                );
            } else {
                log.info("Processed file {} with status {}: {} processed, {} failed", 
                         fileName, status, recordsProcessed, recordsFailed);
                return CompletableFuture.completedFuture(null);
            }
        } catch (IOException e) {
            log.error("Error reading file {}: {}", fileName, e.getMessage(), e);
            
            if (processedFilesTrackingEnabled) {
                return CompletableFuture.completedFuture(
                        processedFileService.markFileAsProcessed(
                                fileName, ProcessedFile.ProcessingStatus.FAILED, recordsProcessed, recordsFailed)
                );
            } else {
                return CompletableFuture.completedFuture(null);
            }
        }
    }

    @Override
    public List<String> findNewFiles(List<String> allFiles) {
        if (!processedFilesTrackingEnabled) {
            return allFiles; // Process all files if tracking is disabled
        }
        
        List<String> processedFiles = processedFileService.getAllProcessedFileNames();
        return allFiles.stream()
                .filter(file -> !processedFiles.contains(file))
                .collect(Collectors.toList());
    }

    private ProcessedFile.ProcessingStatus determineStatus(int recordsProcessed, int recordsFailed) {
        if (recordsProcessed == 0 && recordsFailed > 0) {
            return ProcessedFile.ProcessingStatus.FAILED;
        } else if (recordsProcessed > 0 && recordsFailed > 0) {
            return ProcessedFile.ProcessingStatus.PARTIAL_SUCCESS;
        } else {
            return ProcessedFile.ProcessingStatus.SUCCESS;
        }
    }
} 