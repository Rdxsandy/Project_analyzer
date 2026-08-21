package com.codeguardian.analyzerservice.analyzer;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SourceContextExtractor {

    private static final int CONTEXT_LINES = 20;

    public String extract(
            List<String> lines,
            int issueLine
    ) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }

        int start = Math.max(0, issueLine - 1 - CONTEXT_LINES);
        int end = Math.min(lines.size(), issueLine + CONTEXT_LINES);

        StringBuilder context = new StringBuilder();

        for (int i = start; i < end; i++) {
            context.append(String.format("%4d | %s%n", i + 1, lines.get(i)));
        }

        return context.toString();
    }
}
