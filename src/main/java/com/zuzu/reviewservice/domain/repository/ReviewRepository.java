package com.zuzu.reviewservice.domain.repository;

import com.zuzu.reviewservice.domain.model.Review;
import com.zuzu.reviewservice.domain.model.ReviewProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByProviderReviewIdAndProvider(Long providerReviewId, ReviewProvider provider);
    List<Review> findByHotelId(Integer hotelId);

}