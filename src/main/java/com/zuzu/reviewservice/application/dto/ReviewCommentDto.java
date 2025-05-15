package com.zuzu.reviewservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewCommentDto {
    private Boolean isShowReviewResponse;
    private Integer hotelReviewId;
    private Integer providerId;
    private Double rating;
    private String checkInDateMonthAndYear;
    private String encryptedReviewData;
    private String formattedRating;
    private String formattedReviewDate;
    private String ratingText;
    private String responderName;
    private String responseDateText;
    private String responseTranslateSource;
    private String reviewComments;
    private String reviewNegatives;
    private String reviewPositives;
    private String reviewProviderLogo;
    private String reviewProviderText;
    private String reviewTitle;
    private String translateSource;
    private String translateTarget;
    private String reviewDate;
    private ReviewerInfoDto reviewerInfo;
    private String originalTitle;
    private String originalComment;
    private String formattedResponseDate;
} 