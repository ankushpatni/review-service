package com.zuzu.reviewservice.presentation.controller;

import com.zuzu.reviewservice.application.dto.ReviewDto;
import com.zuzu.reviewservice.application.service.ReviewProcessingService;
import com.zuzu.reviewservice.application.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewProcessingController {

    private final ReviewProcessingService reviewProcessingService;
    private final ReviewService reviewService;


    @PostMapping("/process")
    public ResponseEntity<String> triggerProcessing() {
        log.info("Manual review processing triggered");
        try {
            reviewProcessingService.processNewReviewFiles();
            return ResponseEntity.ok("Review processing completed successfully");
        } catch (Exception e) {
            log.error("Error during manual review processing: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error during review processing: " + e.getMessage());
        }
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<?> getReviewsByHotelId(@PathVariable String hotelId) {
        log.info("Fetching reviews for hotel ID: {}", hotelId);
        try {
            if (hotelId == null || hotelId.isEmpty()) {
                return ResponseEntity.badRequest().body("Hotel ID cannot be null or empty");
            }
            List<ReviewDto> reviews = reviewService.getReviewsByHotelId(Integer.parseInt(hotelId));
            return ResponseEntity.ok(reviews);
        }
        catch (NumberFormatException e) {
            log.error("HotelId can't be String,  hotel ID {}: {}", hotelId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("HotelId can't be String.");
        }
        catch (Exception e) {
            log.error("Error fetching reviews for hotel ID {}: {}", hotelId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error fetching reviews: " + e.getMessage());
        }
    }
} 