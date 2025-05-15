package com.zuzu.reviewservice.domain.repository;

import com.zuzu.reviewservice.domain.model.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, Long> {
    Optional<ProcessedFile> findByFileName(String fileName);
    boolean existsByFileName(String fileName);
} 