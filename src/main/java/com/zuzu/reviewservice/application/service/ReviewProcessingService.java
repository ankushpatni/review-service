package com.zuzu.reviewservice.application.service;

import com.zuzu.reviewservice.domain.model.ProcessedFile;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ReviewProcessingService {
    void processNewReviewFiles();
    CompletableFuture<ProcessedFile> processFile(String fileName, InputStream fileContent);
    List<String> findNewFiles(List<String> allFiles);
} 