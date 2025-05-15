package com.zuzu.reviewservice.domain.repository;

import com.zuzu.reviewservice.domain.model.ReviewProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewProviderRepository extends JpaRepository<ReviewProvider, Integer> {
    Optional<ReviewProvider> findByName(String name);
} 