package com.zuzu.reviewservice.presentation.job;

import com.zuzu.reviewservice.application.service.ReviewProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewProcessingJob {

    private final ReviewProcessingService reviewProcessingService;

    @Value("${review-service.processing.cron}")
    private String cronExpression;

    @Scheduled(cron = "${review-service.processing.cron}")
    public void processReviewFiles() {
        log.info("Starting scheduled review processing job with cron: {}", cronExpression);
        reviewProcessingService.processNewReviewFiles();
        log.info("Completed scheduled review processing job");
    }
}