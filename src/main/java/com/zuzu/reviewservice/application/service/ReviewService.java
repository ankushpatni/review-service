package com.zuzu.reviewservice.application.service;

import com.zuzu.reviewservice.application.dto.ReviewDto;

import java.util.List;

public interface ReviewService {
    void saveReview(ReviewDto reviewDto);
    List<ReviewDto> getReviewsByHotelId(Integer hotelId);
} 