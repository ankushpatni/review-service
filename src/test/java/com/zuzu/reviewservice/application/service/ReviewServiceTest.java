package com.zuzu.reviewservice.application.service;

import com.zuzu.reviewservice.application.dto.ReviewCommentDto;
import com.zuzu.reviewservice.application.dto.ReviewDto;
import com.zuzu.reviewservice.application.dto.ReviewerInfoDto;
import com.zuzu.reviewservice.application.service.impl.ReviewServiceImpl;
import com.zuzu.reviewservice.domain.model.Hotel;
import com.zuzu.reviewservice.domain.model.Review;
import com.zuzu.reviewservice.domain.model.ReviewProvider;
import com.zuzu.reviewservice.domain.repository.HotelRepository;
import com.zuzu.reviewservice.domain.repository.ReviewProviderRepository;
import com.zuzu.reviewservice.domain.repository.ReviewRepository;
import com.zuzu.reviewservice.domain.repository.ReviewResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewResponseRepository reviewResponseRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ReviewProviderRepository reviewProviderRepository;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(
                reviewRepository,
                reviewResponseRepository,
                hotelRepository,
                reviewProviderRepository
        );
    }

    @Test
    void shouldSaveReviewSuccessfully() {
        // Arrange
        Integer hotelId = Integer.valueOf(10984);
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";
        Integer reviewId = Integer.valueOf(948353737);
        double rating = 6.4;

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);

        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(reviewId);
        commentDto.setRating(rating);
        commentDto.setRatingText("Good");
        commentDto.setReviewTitle("Perfect location and safe but hotel under renovation");
        commentDto.setReviewComments("Hotel room is basic and very small. not much like pictures.");
        commentDto.setReviewDate("2025-04-10T05:37:00+07:00");

        ReviewerInfoDto reviewerInfo = new ReviewerInfoDto();
        reviewerInfo.setCountryName("India");
        reviewerInfo.setDisplayMemberName("********");
        reviewerInfo.setReviewGroupName("Solo traveler");
        reviewerInfo.setRoomTypeName("Premium Deluxe Double Room");
        reviewerInfo.setLengthOfStay(2);
        commentDto.setReviewerInfo(reviewerInfo);

        reviewDto.setComment(commentDto);

        // Mock repository responses
        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));
       // when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        ReviewProvider provider = new ReviewProvider(Integer.valueOf(1), platform);
        when(reviewProviderRepository.findByName(platform)).thenReturn(Optional.of(provider));

        when(reviewRepository.findByProviderReviewIdAndProvider(reviewId.longValue(), provider))
                .thenReturn(Optional.empty());

        Review savedReview = new Review();
        savedReview.setId(Integer.valueOf(1));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        // Act
        reviewService.saveReview(reviewDto);

        // Assert
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertEquals(hotelId, capturedReview.getHotel().getId());
        assertEquals(platform, capturedReview.getProvider().getName());
        assertEquals(reviewId, capturedReview.getProviderReviewId().intValue());
        assertEquals(0, BigDecimal.valueOf(rating).compareTo(capturedReview.getRating()));
        assertEquals("Good", capturedReview.getRatingText());
    }

    @Test
    void shouldSkipSavingWhenReviewAlreadyExists() {
        // Arrange
        Integer hotelId = 10984;
        String platform = "Agoda";
        Integer reviewId = Integer.valueOf(948353737);

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setPlatform(platform);

        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(reviewId);
        reviewDto.setComment(commentDto);

        // Mock repository responses
        Hotel hotel = new Hotel(hotelId, "Hotel");
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));

        ReviewProvider provider = new ReviewProvider(Integer.valueOf(1), platform);
        when(reviewProviderRepository.findByName(platform)).thenReturn(Optional.of(provider));

        // Review already exists
        when(reviewRepository.findByProviderReviewIdAndProvider(reviewId.longValue(), provider))
                .thenReturn(Optional.of(new Review()));

        // Act
        reviewService.saveReview(reviewDto);

        // Assert - review save should not be called
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void shouldHandleNullReviewDto() {
        // Act
        reviewService.saveReview(null);

        // Assert - no repository methods should be called
        verifyNoInteractions(hotelRepository, reviewProviderRepository, reviewRepository, reviewResponseRepository);
    }
    
    @Test
    void shouldReturnReviewsForExistingHotelId() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        Hotel hotel = new Hotel(hotelId, hotelName);
        
        ReviewProvider provider = new ReviewProvider(1, "Agoda");
        
        Review review1 = createReview(1, 123456L, hotel, provider, 8.5, "Excellent");
        Review review2 = createReview(2, 789012L, hotel, provider, 7.2, "Good");
        
        when(reviewRepository.findByHotelId(hotelId)).thenReturn(Arrays.asList(review1, review2));
        
        // Act
        List<ReviewDto> result = reviewService.getReviewsByHotelId(hotelId);
        
        // Assert
        assertEquals(2, result.size());
        
        ReviewDto firstReview = result.get(0);
        assertEquals(hotelId, firstReview.getHotelId());
        assertEquals(hotelName, firstReview.getHotelName());
        assertEquals("Agoda", firstReview.getPlatform());
        assertEquals(123456, firstReview.getComment().getHotelReviewId());
        assertEquals(8.5, firstReview.getComment().getRating());
        assertEquals("Excellent", firstReview.getComment().getRatingText());
        
        ReviewDto secondReview = result.get(1);
        assertEquals(hotelId, secondReview.getHotelId());
        assertEquals(789012, secondReview.getComment().getHotelReviewId());
        assertEquals(7.2, secondReview.getComment().getRating());
        assertEquals("Good", secondReview.getComment().getRatingText());
        
        verify(reviewRepository).findByHotelId(hotelId);
    }
    
    @Test
    void shouldReturnEmptyListWhenNoReviewsExist() {
        // Arrange
        Integer hotelId = 10984;
        when(reviewRepository.findByHotelId(hotelId)).thenReturn(Collections.emptyList());
        
        // Act
        List<ReviewDto> result = reviewService.getReviewsByHotelId(hotelId);
        
        // Assert
        assertTrue(result.isEmpty());
        verify(reviewRepository).findByHotelId(hotelId);
    }
    
    @Test
    void shouldReturnEmptyListWhenRepositoryReturnsNull() {
        // Arrange
        Integer hotelId = 10984;
        when(reviewRepository.findByHotelId(hotelId)).thenReturn(null);
        
        // Act
        List<ReviewDto> result = reviewService.getReviewsByHotelId(hotelId);
        
        // Assert
        assertTrue(result.isEmpty());
        verify(reviewRepository).findByHotelId(hotelId);
    }
    
    // Helper method to create test reviews
    private Review createReview(Integer id, Long providerReviewId, Hotel hotel, 
                                ReviewProvider provider, double rating, String ratingText) {
        Review review = new Review();
        review.setId(id);
        review.setProviderReviewId(providerReviewId);
        review.setHotel(hotel);
        review.setProvider(provider);
        review.setRating(BigDecimal.valueOf(rating));
        review.setRatingText(ratingText);
        review.setReviewDate(LocalDateTime.now());
        review.setReviewTitle("Test Review Title");
        review.setReviewComments("Test Review Comments");
        return review;
    }

    @Test
    void shouldHandleProviderNotFound() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        String unknownPlatform = "UnknownPlatform";
        
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(unknownPlatform);
        
        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(123456);
        reviewDto.setComment(commentDto);
        
        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));
        when(reviewProviderRepository.findByName(unknownPlatform)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.saveReview(reviewDto);
        });
        
        assertTrue(exception.getMessage().contains("Error saving review data"));
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertTrue(exception.getCause().getMessage().contains("Provider not found"));
        
        verify(reviewProviderRepository).findByName(unknownPlatform);
        verify(reviewRepository, never()).save(any());
    }
    
    @Test
    void shouldHandleHotelRepositorySaveException() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";
        
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);
        
        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(123456);
        reviewDto.setComment(commentDto);
        
        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.empty());
        when(hotelRepository.save(any(Hotel.class))).thenThrow(new RuntimeException("Database connection failed"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.saveReview(reviewDto);
        });
        
        assertTrue(exception.getMessage().contains("Error saving review data"));
        assertTrue(exception.getCause().getMessage().contains("Database connection failed"));
        
        verify(hotelRepository).save(any(Hotel.class));
        verify(reviewRepository, never()).save(any());
    }
    
    @Test
    void shouldHandleReviewRepositorySaveException() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";
        Integer reviewId = 123456;
        
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);
        
        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(reviewId);
        commentDto.setRating(8.5);
        commentDto.setRatingText("Good");
        reviewDto.setComment(commentDto);
        
        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));
        
        ReviewProvider provider = new ReviewProvider(1, platform);
        when(reviewProviderRepository.findByName(platform)).thenReturn(Optional.of(provider));
        
        when(reviewRepository.findByProviderReviewIdAndProvider(reviewId.longValue(), provider))
                .thenReturn(Optional.empty());
        
        when(reviewRepository.save(any(Review.class))).thenThrow(new RuntimeException("Failed to save review"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.saveReview(reviewDto);
        });
        
        assertTrue(exception.getMessage().contains("Error saving review data"));
        assertTrue(exception.getCause().getMessage().contains("Failed to save review"));
        
        verify(reviewRepository).save(any(Review.class));
    }
    
    @Test
    void shouldHandleReviewResponseRepositorySaveException() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";
        Integer reviewId = 123456;
        
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);
        
        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(reviewId);
        commentDto.setRating(8.5);
        commentDto.setRatingText("Good");
        commentDto.setResponderName("Hotel Manager");  // This will trigger response save
        commentDto.setResponseDateText("Thank you for your feedback");
        reviewDto.setComment(commentDto);
        
        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));
        
        ReviewProvider provider = new ReviewProvider(1, platform);
        when(reviewProviderRepository.findByName(platform)).thenReturn(Optional.of(provider));
        
        when(reviewRepository.findByProviderReviewIdAndProvider(reviewId.longValue(), provider))
                .thenReturn(Optional.empty());
        
        Review savedReview = new Review();
        savedReview.setId(1);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        
        when(reviewResponseRepository.save(any())).thenThrow(new RuntimeException("Failed to save response"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.saveReview(reviewDto);
        });
        
        assertTrue(exception.getMessage().contains("Error saving review data"));
        assertTrue(exception.getCause().getMessage().contains("Failed to save response"));
        
        verify(reviewRepository).save(any(Review.class));
        verify(reviewResponseRepository).save(any());
    }

    @Test
    void shouldHandleMissingRatingInReview() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";
        Integer reviewId = 123456;

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);

        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(reviewId);
        commentDto.setRating(null); // Missing rating
        commentDto.setRatingText("Good");
        reviewDto.setComment(commentDto);

        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));

        ReviewProvider provider = new ReviewProvider(1, platform);
        when(reviewProviderRepository.findByName(platform)).thenReturn(Optional.of(provider));

        when(reviewRepository.findByProviderReviewIdAndProvider(reviewId.longValue(), provider))
                .thenReturn(Optional.empty());

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            savedReview.setId(1);
            return savedReview;
        });

        // Act
        reviewService.saveReview(reviewDto);

        // Assert
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertNull(capturedReview.getRating());
    }
    
    @Test
    void shouldHandleInvalidReviewDate() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";
        Integer reviewId = 123456;

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);

        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(reviewId);
        commentDto.setRating(8.5);
        commentDto.setRatingText("Good");
        commentDto.setReviewDate("Invalid date format"); // Invalid date format
        reviewDto.setComment(commentDto);

        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));

        ReviewProvider provider = new ReviewProvider(1, platform);
        when(reviewProviderRepository.findByName(platform)).thenReturn(Optional.of(provider));

        when(reviewRepository.findByProviderReviewIdAndProvider(reviewId.longValue(), provider))
                .thenReturn(Optional.empty());

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            savedReview.setId(1);
            return savedReview;
        });

        // Act
        reviewService.saveReview(reviewDto);

        // Assert
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertNotNull(capturedReview.getReviewDate(), "Should use current date as fallback");
    }
    
    @Test
    void shouldHandleMissingReviewDate() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";
        Integer reviewId = 123456;

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);

        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(reviewId);
        commentDto.setRating(8.5);
        commentDto.setRatingText("Good");
        commentDto.setReviewDate(null); // Null review date
        reviewDto.setComment(commentDto);

        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));

        ReviewProvider provider = new ReviewProvider(1, platform);
        when(reviewProviderRepository.findByName(platform)).thenReturn(Optional.of(provider));

        when(reviewRepository.findByProviderReviewIdAndProvider(reviewId.longValue(), provider))
                .thenReturn(Optional.empty());

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            savedReview.setId(1);
            return savedReview;
        });

        // Act
        reviewService.saveReview(reviewDto);

        // Assert
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertNotNull(capturedReview.getReviewDate(), "Should use current date as fallback");
    }
    
    @Test
    void shouldHandleEmptyReviewDate() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";
        Integer reviewId = 123456;

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);

        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(reviewId);
        commentDto.setRating(8.5);
        commentDto.setRatingText("Good");
        commentDto.setReviewDate(""); // Empty review date
        reviewDto.setComment(commentDto);

        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));

        ReviewProvider provider = new ReviewProvider(1, platform);
        when(reviewProviderRepository.findByName(platform)).thenReturn(Optional.of(provider));

        when(reviewRepository.findByProviderReviewIdAndProvider(reviewId.longValue(), provider))
                .thenReturn(Optional.empty());

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            savedReview.setId(1);
            return savedReview;
        });

        // Act
        reviewService.saveReview(reviewDto);

        // Assert
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertNotNull(capturedReview.getReviewDate(), "Should use current date as fallback");
    }
    
    /*@Test
    void shouldHandleReviewWithMissingHotelId() {
        // Arrange
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(null); // Missing hotel ID
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);

        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(123456);
        reviewDto.setComment(commentDto);

        // Act
        reviewService.saveReview(reviewDto);

        // Assert - should handle gracefully without exceptions
        verify(hotelRepository, never()).findById(any());
        verify(reviewRepository, never()).save(any());
    }*/
    
    @Test
    void shouldHandleCompleteReviewerInfo() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";
        Integer reviewId = 123456;

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);

        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(reviewId);
        commentDto.setRating(8.5);
        commentDto.setRatingText("Good");
        commentDto.setReviewDate("2023-01-15T10:30:00+07:00");

        // Add complete reviewer info
        ReviewerInfoDto reviewerInfo = new ReviewerInfoDto();
        reviewerInfo.setCountryName("India");
        reviewerInfo.setDisplayMemberName("John D.");
        reviewerInfo.setReviewGroupName("Family with young children");
        reviewerInfo.setRoomTypeName("Deluxe Room");
        reviewerInfo.setLengthOfStay(5);
        commentDto.setReviewerInfo(reviewerInfo);

        reviewDto.setComment(commentDto);

        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));

        ReviewProvider provider = new ReviewProvider(1, platform);
        when(reviewProviderRepository.findByName(platform)).thenReturn(Optional.of(provider));

        when(reviewRepository.findByProviderReviewIdAndProvider(reviewId.longValue(), provider))
                .thenReturn(Optional.empty());

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            savedReview.setId(1);
            return savedReview;
        });

        // Act
        reviewService.saveReview(reviewDto);

        // Assert
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertEquals("India", capturedReview.getReviewerCountry());
        assertEquals("John D.", capturedReview.getReviewerName());
        assertEquals("Family with young children", capturedReview.getReviewerType());
        assertEquals("Deluxe Room", capturedReview.getRoomType());
        assertEquals(Integer.valueOf(5), capturedReview.getLengthOfStay());
    }
    
    @Test
    void shouldHandleNullReviewerInfo() {
        // Arrange
        Integer hotelId = 10984;
        String hotelName = "Oscar Saigon Hotel";
        String platform = "Agoda";
        Integer reviewId = 123456;

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setHotelId(hotelId);
        reviewDto.setHotelName(hotelName);
        reviewDto.setPlatform(platform);

        ReviewCommentDto commentDto = new ReviewCommentDto();
        commentDto.setHotelReviewId(reviewId);
        commentDto.setRating(8.5);
        commentDto.setRatingText("Good");
        commentDto.setReviewerInfo(null); // Null reviewer info
        reviewDto.setComment(commentDto);

        Hotel hotel = new Hotel(hotelId, hotelName);
        when(hotelRepository.findById(hotelId.longValue())).thenReturn(Optional.of(hotel));

        ReviewProvider provider = new ReviewProvider(1, platform);
        when(reviewProviderRepository.findByName(platform)).thenReturn(Optional.of(provider));

        when(reviewRepository.findByProviderReviewIdAndProvider(reviewId.longValue(), provider))
                .thenReturn(Optional.empty());

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            savedReview.setId(1);
            return savedReview;
        });

        // Act
        reviewService.saveReview(reviewDto);

        // Assert
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertNull(capturedReview.getReviewerCountry());
        assertNull(capturedReview.getReviewerName());
        assertNull(capturedReview.getReviewerType());
        assertNull(capturedReview.getRoomType());
        assertNull(capturedReview.getLengthOfStay());
    }
} 