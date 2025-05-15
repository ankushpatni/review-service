package com.zuzu.reviewservice.domain.repository;

import com.zuzu.reviewservice.domain.model.Review;
import com.zuzu.reviewservice.domain.model.ReviewResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewResponseRepository extends JpaRepository<ReviewResponse, Long> {
    Optional<ReviewResponse> findByReview(Review review);
} 