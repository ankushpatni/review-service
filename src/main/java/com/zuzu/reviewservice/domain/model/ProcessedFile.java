package com.zuzu.reviewservice.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "file_name", nullable = false, unique = true)
    private String fileName;

    @Column(name = "processed_date", nullable = false)
    private LocalDateTime processedDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;

    @Column(name = "records_processed", nullable = false)
    private int recordsProcessed;

    @Column(name = "records_failed", nullable = false)
    private int recordsFailed;

    public enum ProcessingStatus {
        SUCCESS, PARTIAL_SUCCESS, FAILED
    }
} 