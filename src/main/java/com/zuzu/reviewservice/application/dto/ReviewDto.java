package com.zuzu.reviewservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewDto {
    private Integer hotelId;
    private String platform;
    private String hotelName;
    private ReviewCommentDto comment;
    private List<ProviderScoreDto> overallByProviders;
} 