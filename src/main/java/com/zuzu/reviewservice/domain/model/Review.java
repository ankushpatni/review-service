package com.zuzu.reviewservice.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "provider_review_id", nullable = false)
    private Long providerReviewId;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private ReviewProvider provider;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "rating", nullable = false)
    private BigDecimal rating;

    @Column(name = "rating_text")
    private String ratingText;

    @Column(name = "review_date", nullable = false)
    private LocalDateTime reviewDate;

    @Column(name = "review_title")
    private String reviewTitle;

    @Column(name = "review_comments")
    private String reviewComments;

    @Column(name = "review_positives")
    private String reviewPositives;

    @Column(name = "review_negatives")
    private String reviewNegatives;

    @Column(name = "reviewer_country")
    private String reviewerCountry;

    @Column(name = "reviewer_name")
    private String reviewerName;

    @Column(name = "reviewer_type")
    private String reviewerType;

    @Column(name = "room_type")
    private String roomType;

    @Column(name = "length_of_stay")
    private Integer lengthOfStay;

    @Column(name = "check_in_date")
    private String checkInDate;

    @Column(name = "formatted_rating")
    private String formattedRating;

    @Column(name = "translate_source")
    private String translateSource;

    @Column(name = "translate_target")
    private String translateTarget;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
} 