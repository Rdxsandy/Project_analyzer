package com.codeguardian.scanservice.dto;

public record AIReviewResponse(
        Long id,
        String rule,
        boolean valid,
        String confidence,
        String explanation,
        String recommendation
) {}
