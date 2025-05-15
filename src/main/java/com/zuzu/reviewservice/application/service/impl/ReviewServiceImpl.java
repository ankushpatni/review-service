package com.zuzu.reviewservice.application.service.impl;

import com.zuzu.reviewservice.application.dto.ReviewCommentDto;
import com.zuzu.reviewservice.application.dto.ReviewDto;
import com.zuzu.reviewservice.application.dto.ReviewerInfoDto;
import com.zuzu.reviewservice.application.service.ReviewService;
import com.zuzu.reviewservice.domain.model.Hotel;
import com.zuzu.reviewservice.domain.model.Review;
import com.zuzu.reviewservice.domain.model.ReviewProvider;
import com.zuzu.reviewservice.domain.model.ReviewResponse;
import com.zuzu.reviewservice.domain.repository.HotelRepository;
import com.zuzu.reviewservice.domain.repository.ReviewProviderRepository;
import com.zuzu.reviewservice.domain.repository.ReviewRepository;
import com.zuzu.reviewservice.domain.repository.ReviewResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewResponseRepository reviewResponseRepository;
    private final HotelRepository hotelRepository;
    private final ReviewProviderRepository reviewProviderRepository;

    @Override
    @Transactional
    public void saveReview(ReviewDto reviewDto) {
        try {
            if (reviewDto == null || reviewDto.getComment() == null) {
                log.warn("Invalid review data, skipping");
                return;
            }

            ReviewCommentDto commentDto = reviewDto.getComment();
            /*if(reviewDto.getHotelName() == null || reviewDto.getHotelName().isEmpty()) {
                log.warn("Hotel name is missing, skipping review");
                return;
            }*/
            // 1. Get or create hotel
            Optional<Hotel> hotel = hotelRepository.findById(reviewDto.getHotelId().longValue());
            if(hotel.isEmpty()) {
                hotel = Optional.ofNullable(Hotel.builder()
                        .id(reviewDto.getHotelId().intValue())
                        .name(reviewDto.getHotelName())
                        .build());
                hotel = Optional.of(hotelRepository.save(hotel.get()));
            }

            // 2. Get provider
            ReviewProvider provider = reviewProviderRepository.findByName(reviewDto.getPlatform())
                    .orElseThrow(() -> new IllegalStateException(
                            "Provider not found: " + reviewDto.getPlatform()));

            // 3. Check if review already exists
            Optional<Review> existingReview = reviewRepository.findByProviderReviewIdAndProvider(
                    commentDto.getHotelReviewId().longValue(), provider);
            
            if (existingReview.isPresent()) {
                log.debug("Review already exists, skipping: {} for provider {}",
                        commentDto.getHotelReviewId(), provider.getName());
                return;
            }

            // 4. Parse dates
            LocalDateTime reviewDate = null;
            if (commentDto.getReviewDate() != null && !commentDto.getReviewDate().isEmpty()) {
                try {
                    reviewDate = ZonedDateTime.parse(commentDto.getReviewDate(), 
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
                } catch (Exception e) {
                    log.warn("Could not parse review date: {}", commentDto.getReviewDate());
                    reviewDate = LocalDateTime.now(); // Fallback
                }
            } else {
                reviewDate = LocalDateTime.now(); // Fallback
            }

            // 5. Create and save review
            Review review = Review.builder()
                    .providerReviewId(commentDto.getHotelReviewId().longValue())
                    .provider(provider)
                    .hotel(hotel.get())
                    .rating(commentDto.getRating() != null ? 
                            BigDecimal.valueOf(commentDto.getRating()) : null)
                    .ratingText(commentDto.getRatingText())
                    .reviewDate(reviewDate)
                    .reviewTitle(commentDto.getReviewTitle())
                    .reviewComments(commentDto.getReviewComments())
                    .reviewPositives(commentDto.getReviewPositives())
                    .reviewNegatives(commentDto.getReviewNegatives())
                    .formattedRating(commentDto.getFormattedRating())
                    .translateSource(commentDto.getTranslateSource())
                    .translateTarget(commentDto.getTranslateTarget())
                    .checkInDate(commentDto.getCheckInDateMonthAndYear())
                    .build();

            // 6. Add reviewer information if available
            ReviewerInfoDto reviewerInfo = commentDto.getReviewerInfo();
            if (reviewerInfo != null) {
                review.setReviewerCountry(reviewerInfo.getCountryName());
                review.setReviewerName(reviewerInfo.getDisplayMemberName());
                review.setReviewerType(reviewerInfo.getReviewGroupName());
                review.setRoomType(reviewerInfo.getRoomTypeName());
                review.setLengthOfStay(reviewerInfo.getLengthOfStay());
            }

            review = reviewRepository.save(review);
            
            // 7. Save response if exists
            if (commentDto.getResponderName() != null && !commentDto.getResponderName().isEmpty()) {
                LocalDateTime responseDate = null;
                if (commentDto.getFormattedResponseDate() != null && 
                        !commentDto.getFormattedResponseDate().isEmpty()) {
                    try {
                        // Try to parse formatted date, but this is often not in a standard format
                        responseDate = LocalDateTime.now(); // Fallback
                    } catch (Exception e) {
                        responseDate = LocalDateTime.now();
                    }
                } else {
                    responseDate = LocalDateTime.now();
                }
                
                ReviewResponse response = ReviewResponse.builder()
                        .review(review)
                        .responderName(commentDto.getResponderName())
                        .responseDate(responseDate)
                        .responseText(commentDto.getResponseDateText())
                        .build();
                
                reviewResponseRepository.save(response);
            }
            
            log.debug("Successfully saved review: {} for hotel: {}", 
                    commentDto.getHotelReviewId(), reviewDto.getHotelId());
            
        } catch (Exception e) {
            log.error("Error saving review: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving review data", e);
        }
    }

    @Override
    public List<ReviewDto> getReviewsByHotelId(Integer hotelId) {
        List<Review> reviews = reviewRepository.findByHotelId(hotelId);

        if (reviews == null || reviews.isEmpty()) {
            log.warn("No reviews found for hotel ID: {}", hotelId);
            return List.of();
        }

        return reviews.stream().map(review -> ReviewDto.builder()
                        .hotelId(review.getHotel().getId())
                        .hotelName(review.getHotel().getName())
                        .platform(review.getProvider().getName())
                        .comment(ReviewCommentDto.builder()
                                .hotelReviewId(review.getProviderReviewId().intValue())
                                .reviewTitle(review.getReviewTitle())
                                .reviewComments(review.getReviewComments())
                                .reviewPositives(review.getReviewPositives())
                                .reviewNegatives(review.getReviewNegatives())
                                .rating(review.getRating() != null ? review.getRating().doubleValue() : null)
                                .ratingText(review.getRatingText())
                                .reviewDate(review.getReviewDate() != null ? review.getReviewDate().toString() : null)
                                .build())
                        .build())
                .toList();
    }
} 