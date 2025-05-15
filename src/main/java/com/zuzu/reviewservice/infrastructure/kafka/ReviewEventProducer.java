package com.zuzu.reviewservice.infrastructure.kafka;

import com.zuzu.reviewservice.application.dto.ReviewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewEventProducer {

    private final KafkaTemplate<String, ReviewDto> kafkaTemplate;

    @Value("${spring.kafka.topics.review-events:review-events}")
    private String reviewEventsTopic;

    /**
     * Publishes a review event to Kafka
     * @param reviewDto The review data to publish
     * @return CompletableFuture that completes when the message is sent
     */
    public CompletableFuture<SendResult<String, ReviewDto>> publishReviewEvent(ReviewDto reviewDto) {
        String key = generateKey(reviewDto);
        log.debug("Publishing review event with key: {} to topic: {}", key, reviewEventsTopic);
        
        CompletableFuture<SendResult<String, ReviewDto>> future = 
                kafkaTemplate.send(reviewEventsTopic, key, reviewDto);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Review event sent successfully: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send review event: {}", ex.getMessage(), ex);
            }
        });
        
        return future;
    }
    
    /**
     * Generates a unique key for the review message
     * Using hotelId and reviewId if available, otherwise a UUID
     */
    private String generateKey(ReviewDto reviewDto) {
        if (reviewDto.getHotelId() != null && reviewDto.getComment() != null && 
                reviewDto.getComment().getHotelReviewId() != null) {
            return reviewDto.getHotelId() + "-" + reviewDto.getComment().getHotelReviewId();
        }
        return UUID.randomUUID().toString();
    }
} 