package com.zuzu.reviewservice.domain.repository;

import com.zuzu.reviewservice.domain.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
}