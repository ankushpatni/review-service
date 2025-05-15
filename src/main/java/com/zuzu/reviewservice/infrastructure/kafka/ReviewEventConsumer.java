package com.zuzu.reviewservice.infrastructure.kafka;

import com.zuzu.reviewservice.application.dto.ReviewDto;
import com.zuzu.reviewservice.application.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewEventConsumer {

    private final ReviewService reviewService;

    /**
     * Consumes review events from Kafka and processes them
     * @param reviewDto The review data to process
     * @param topic The Kafka topic
     * @param partition The Kafka partition
     * @param offset The message offset
     * @param timestamp The message timestamp
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.review-events:review-events}",
            groupId = "${spring.kafka.consumer.group-id:review-service-group}"
    )
    public void consumeReviewEvent(
            @Payload ReviewDto reviewDto,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) {
        
        log.debug("Received review event: topic={}, partition={}, offset={}, timestamp={}",
                topic, partition, offset, timestamp);
        
        try {
            // Process the review using the existing service
            reviewService.saveReview(reviewDto);
            log.debug("Successfully processed review event for hotel ID: {}, review ID: {}",
                    reviewDto.getHotelId(), 
                    reviewDto.getComment() != null ? reviewDto.getComment().getHotelReviewId() : null);
        } catch (Exception e) {
            log.error("Error processing review event: {}", e.getMessage(), e);
            // In a production system, you might want to implement retry logic or dead letter queue
        }
    }
} 