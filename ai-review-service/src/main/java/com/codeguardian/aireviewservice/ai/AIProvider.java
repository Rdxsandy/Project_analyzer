package com.codeguardian.aireviewservice.ai;

import com.codeguardian.aireviewservice.model.AIReviewRequest;
import com.codeguardian.aireviewservice.model.AIReviewedIssue;

public interface AIProvider {

    AIReviewedIssue review(
            AIReviewRequest request
    );
}
