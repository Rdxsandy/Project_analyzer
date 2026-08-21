package com.codeguardian.analyzerservice.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AnalysisLogger {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AnalysisLogger.class
            );

    public void started(
            Long scanId,
            String repository
    ) {

        log.info(
                "Starting scan {} for repository {}",
                scanId,
                repository
        );
    }

    public void completed(
            Long scanId,
            int files,
            int issues
    ) {

        log.info(
                "Completed scan {}. Files={}, Issues={}",
                scanId,
                files,
                issues
        );
    }
}
