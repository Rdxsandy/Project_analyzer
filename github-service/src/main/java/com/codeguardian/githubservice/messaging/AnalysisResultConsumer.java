package com.codeguardian.githubservice.messaging;

import com.codeguardian.githubservice.github.GitHubApiClient;
import com.codeguardian.githubservice.github.PullRequestCommentFormatter;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AnalysisResultConsumer {

    private final GitHubApiClient githubApiClient;
    private final PullRequestCommentFormatter formatter;

    public AnalysisResultConsumer(
            GitHubApiClient githubApiClient,
            PullRequestCommentFormatter formatter
    ) {
        this.githubApiClient = githubApiClient;
        this.formatter = formatter;
    }

    @RabbitListener(
            queues = RabbitMQConstants.RESULT_QUEUE
    )
    public void consume(
            AnalysisResultMessage result
    ) throws Exception {

        if (result.getPullRequestNumber() == null || result.getPullRequestNumber() == 0L) {

            System.out.println(
                    "Full scan result received. "
                            + "No PR feedback required."
            );

            return;
        }

        String comment =
                formatter.format(result);

        githubApiClient.createPullRequestComment(
                result.getOwner(),
                result.getRepository(),
                result.getPullRequestNumber(),
                comment
        );

        System.out.println(
                "GitHub PR feedback posted for PR #"
                        + result.getPullRequestNumber()
        );
    }
}
