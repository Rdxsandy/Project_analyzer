package com.codeguardian.scanservice.repository;

import com.codeguardian.scanservice.entity.AIReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIReviewRepository extends JpaRepository<AIReview, Long> {

    List<AIReview> findByScan_Id(Long scanId);
}
