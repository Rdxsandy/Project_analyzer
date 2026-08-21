package com.codeguardian.githubservice.github;

import com.codeguardian.githubservice.messaging.AnalysisIssueMessage;
import com.codeguardian.githubservice.messaging.AnalysisResultMessage;
import org.springframework.stereotype.Component;

@Component
public class PullRequestCommentFormatter {

    public String format(
            AnalysisResultMessage result
    ) {

        StringBuilder body =
                new StringBuilder();

        body.append("## 🛡️ AI Code Guardian\n\n");

        body.append(
                "**Analysis completed**\n\n"
        );

        body.append(
                "**Total issues:** "
                        + result.getTotalIssues()
                        + "\n\n"
        );

        if (result.getIssues() == null
                || result.getIssues().isEmpty()) {

            body.append(
                    "### ✅ No issues detected\n\n"
            );

            body.append(
                    "No issues were detected by the current static analysis rules."
            );

            return body.toString();
        }

        body.append(
                "### Issues\n\n"
        );

        for (AnalysisIssueMessage issue :
                result.getIssues()) {

            body.append("- **")
                    .append(issue.getSeverity())
                    .append("** `")
                    .append(issue.getRule())
                    .append("` — ")
                    .append(issue.getMessage())
                    .append("\n");

            body.append("  - File: `")
                    .append(issue.getFile())
                    .append(":")
                    .append(issue.getLine())
                    .append("`\n");

            if (issue.getRecommendation() != null) {

                body.append(
                        "  - Recommendation: "
                )
                        .append(
                                issue.getRecommendation()
                        )
                        .append("\n");
            }

            body.append("\n");
        }

        return body.toString();
    }
}
