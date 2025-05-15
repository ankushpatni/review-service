package com.zuzu.reviewservice.application.service;

import com.zuzu.reviewservice.domain.model.ProcessedFile;

import java.util.List;

public interface ProcessedFileService {
    ProcessedFile markFileAsProcessed(String fileName, ProcessedFile.ProcessingStatus status, 
                                     int recordsProcessed, int recordsFailed);
    boolean isFileProcessed(String fileName);
    List<String> getAllProcessedFileNames();
}