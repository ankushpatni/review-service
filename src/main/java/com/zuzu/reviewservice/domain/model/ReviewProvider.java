package com.zuzu.reviewservice.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;


@Entity
@Table(name = "review_providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
} 