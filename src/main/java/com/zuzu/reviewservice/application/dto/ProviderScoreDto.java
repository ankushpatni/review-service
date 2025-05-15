package com.zuzu.reviewservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderScoreDto {
    private Integer providerId;
    private String provider;
    private BigDecimal overallScore;
    private Integer reviewCount;
    private Map<String, BigDecimal> grades;
} 