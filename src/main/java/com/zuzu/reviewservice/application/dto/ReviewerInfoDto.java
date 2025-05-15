package com.zuzu.reviewservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewerInfoDto {
    private String countryName;
    private String displayMemberName;
    private String flagName;
    private String reviewGroupName;
    private String roomTypeName;
    private Integer countryId;
    private Integer lengthOfStay;
    private Integer reviewGroupId;
    private Integer roomTypeId;
    private Integer reviewerReviewedCount;
    private Boolean isExpertReviewer;
    private Boolean isShowGlobalIcon;
    private Boolean isShowReviewedCount;
} 