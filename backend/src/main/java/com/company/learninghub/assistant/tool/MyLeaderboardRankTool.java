package com.company.learninghub.assistant.tool;

import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.intent.ResolvedIntent;
import com.company.learninghub.leaderboard.dto.PersonalLeaderboardResponse;
import com.company.learninghub.leaderboard.service.LeaderboardService;
import org.springframework.stereotype.Component;

@Component
public class MyLeaderboardRankTool implements AssistantTool {

    private final LeaderboardService leaderboardService;

    public MyLeaderboardRankTool(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Override
    public String getName() {
        return AssistantToolNames.MY_LEADERBOARD_RANK;
    }

    @Override
    public boolean supports(ResolvedIntent intent) {
        return intent.type() == AssistantIntentType.TOOL
                && AssistantToolNames.MY_LEADERBOARD_RANK.equals(intent.toolName());
    }

    @Override
    public ToolResult execute(AssistantToolContext context) {
        PersonalLeaderboardResponse ranking = leaderboardService.getPersonalRanking(context.authenticatedUser());
        String text = ranking.globalRank() == null
                ? "You do not have an approved certification rank yet."
                : "Your global leaderboard rank is #%d with %d approved certification(s)."
                        .formatted(ranking.globalRank(), ranking.totalApprovedCertifications());
        return ToolResult.structured(text, ranking);
    }
}
