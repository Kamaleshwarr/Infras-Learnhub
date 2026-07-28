package com.company.learninghub.assistant.intent;

import com.company.learninghub.assistant.tool.AssistantToolNames;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class IntentResolver {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s]+");

    private final NavigationIntentResolver navigationIntentResolver;

    public IntentResolver(NavigationIntentResolver navigationIntentResolver) {
        this.navigationIntentResolver = navigationIntentResolver;
    }

    public ResolvedIntent resolve(String message) {
        String normalized = normalize(message);
        if (!StringUtils.hasText(normalized)) {
            return ResolvedIntent.unknown(normalized);
        }

        Optional<NavigationTarget> navigationTarget = navigationIntentResolver.resolve(message);
        if (navigationTarget.isPresent()) {
            return ResolvedIntent.navigation(navigationTarget.get(), normalized);
        }

        Optional<String> toolName = resolveToolName(normalized);
        if (toolName.isPresent()) {
            return ResolvedIntent.tool(toolName.get(), normalized);
        }

        if (isKnowledgeQuery(normalized)) {
            return ResolvedIntent.knowledge(normalized);
        }

        return ResolvedIntent.unknown(normalized);
    }

    private Optional<String> resolveToolName(String normalized) {
        if (matchesAny(
                normalized,
                "my profile",
                "show my profile",
                "view my profile",
                "profile details"
        )) {
            return Optional.of(AssistantToolNames.MY_PROFILE);
        }
        if (matchesAny(
                normalized,
                "my leaderboard rank",
                "my rank",
                "leaderboard rank",
                "show my rank",
                "what is my rank"
        )) {
            return Optional.of(AssistantToolNames.MY_LEADERBOARD_RANK);
        }
        if (matchesAny(
                normalized,
                "my certifications",
                "my certification",
                "my certificates",
                "show my certifications",
                "list my certifications"
        )) {
            return Optional.of(AssistantToolNames.MY_CERTIFICATIONS);
        }
        if (matchesAny(
                normalized,
                "available learning initiatives",
                "learning initiatives",
                "available initiatives",
                "show learning initiatives",
                "list learning initiatives"
        )) {
            return Optional.of(AssistantToolNames.AVAILABLE_LEARNING_INITIATIVES);
        }

        return Optional.empty();
    }

    private boolean isKnowledgeQuery(String normalized) {
        return matchesAny(
                normalized,
                "what is",
                "what are",
                "how do i",
                "how does",
                "explain",
                "tell me about",
                "describe"
        );
    }

    private boolean matchesAny(String normalized, String... phrases) {
        for (String phrase : phrases) {
            if (normalized.equals(phrase) || normalized.contains(phrase)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String message) {
        if (!StringUtils.hasText(message)) {
            return "";
        }
        String normalized = message.trim().toLowerCase(Locale.ROOT);
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll(" ");
        return normalized.replaceAll("\\s+", " ").trim();
    }
}
