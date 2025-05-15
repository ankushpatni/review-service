package com.zuzu.reviewservice.application.service.impl;

import com.zuzu.reviewservice.application.service.ProcessedFileService;
import com.zuzu.reviewservice.domain.model.ProcessedFile;
import com.zuzu.reviewservice.domain.repository.ProcessedFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessedFileServiceImpl implements ProcessedFileService {

    private final ProcessedFileRepository processedFileRepository;

    @Override
    @Transactional
    public ProcessedFile markFileAsProcessed(String fileName, ProcessedFile.ProcessingStatus status,
                                           int recordsProcessed, int recordsFailed) {
        ProcessedFile processedFile = ProcessedFile.builder()
                .fileName(fileName)
                .processedDate(LocalDateTime.now())
                .status(status)
                .recordsProcessed(recordsProcessed)
                .recordsFailed(recordsFailed)
                .build();

        return processedFileRepository.save(processedFile);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFileProcessed(String fileName) {
        return processedFileRepository.existsByFileName(fileName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllProcessedFileNames() {
        return processedFileRepository.findAll().stream()
                .map(ProcessedFile::getFileName)
                .collect(Collectors.toList());
    }
} 