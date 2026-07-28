package com.company.learninghub.assistant.intent;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class NavigationIntentResolver {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s]+");

    public Optional<NavigationTarget> resolve(String message) {
        String normalized = normalize(message);
        if (!StringUtils.hasText(normalized)) {
            return Optional.empty();
        }

        if (matchesAny(normalized, "open projects", "go to projects", "projects page", "show projects")) {
            return Optional.of(new NavigationTarget("/projects", "Projects"));
        }
        if (matchesAny(normalized, "open learn", "go to learn", "learn page", "show learn", "learning catalog")) {
            return Optional.of(new NavigationTarget("/learn", "Learn"));
        }
        if (matchesAny(
                normalized,
                "open leaderboards",
                "go to leaderboards",
                "open leaderboard",
                "go to leaderboard",
                "leaderboards page",
                "show leaderboards"
        )) {
            return Optional.of(new NavigationTarget("/leaderboards/global", "Leaderboards"));
        }
        if (matchesAny(normalized, "open dashboard", "go to dashboard", "dashboard", "home page", "go home")) {
            return Optional.of(new NavigationTarget("/", "Dashboard"));
        }

        return Optional.empty();
    }

    private boolean matchesAny(String normalized, String... phrases) {
        for (String phrase : phrases) {
            if (normalized.equals(phrase) || normalized.contains(phrase)) {
                return true;
            }
        }
        return false;
    }

    String normalize(String message) {
        if (!StringUtils.hasText(message)) {
            return "";
        }
        String normalized = message.trim().toLowerCase(Locale.ROOT);
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll(" ");
        return normalized.replaceAll("\\s+", " ").trim();
    }
}
