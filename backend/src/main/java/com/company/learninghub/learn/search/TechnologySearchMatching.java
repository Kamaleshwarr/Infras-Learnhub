package com.company.learninghub.learn.search;

import com.company.learninghub.learn.domain.LearnTechnology;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic catalog search matching and relevance scoring for technology discovery.
 *
 * @see TechnologySearchRelevance for ranked match tiers
 */
public final class TechnologySearchMatching {

    private TechnologySearchMatching() {
    }

    public static boolean matches(
            String name,
            String shortName,
            String description,
            String slug,
            List<String> tags,
            String query
    ) {
        return score(name, shortName, description, slug, tags, query) > 0;
    }

    public static boolean matches(LearnTechnology technology, String query) {
        return score(technology, query) > 0;
    }

    public static int score(LearnTechnology technology, String query) {
        return score(
                technology.getName(),
                technology.getShortName(),
                technology.getDescription(),
                technology.getSlug(),
                technology.getTags(),
                query
        );
    }

    public static int score(
            String name,
            String shortName,
            String description,
            String slug,
            List<String> tags,
            String query
    ) {
        List<String> tokens = tokenize(query);
        if (tokens.isEmpty()) {
            return 0;
        }

        if (!matchesAllTokens(name, shortName, description, slug, tags, tokens)) {
            return 0;
        }

        String normalizedQuery = normalizePhrase(query);
        String lowerName = normalizeField(name);
        String lowerShortName = normalizeField(shortName);
        String lowerDescription = normalizeField(description);
        String lowerSlug = normalizeField(slug);
        String slugAsPhrase = lowerSlug.replace('-', ' ');

        int score = 0;

        if (lowerName.equals(normalizedQuery)) {
            score = Math.max(score, TechnologySearchRelevance.EXACT_NAME_MATCH.score());
        }
        if (phraseStartsWith(lowerName, normalizedQuery)) {
            score = Math.max(score, TechnologySearchRelevance.NAME_STARTS_WITH.score());
        }
        if (phraseMatchesAsWords(lowerName, normalizedQuery)) {
            score = Math.max(score, TechnologySearchRelevance.EXACT_NAME_MATCH.score());
        }
        if (phraseMatchesAsWords(lowerShortName, normalizedQuery)) {
            score = Math.max(score, TechnologySearchRelevance.SHORT_NAME_MATCH.score());
        }
        if (phraseMatchesAsWords(slugAsPhrase, normalizedQuery)) {
            score = Math.max(score, TechnologySearchRelevance.SLUG_MATCH.score());
        }

        for (String token : tokens) {
            if (wordEquals(lowerName, token)) {
                score = Math.max(score, TechnologySearchRelevance.EXACT_NAME_MATCH.score());
            }
            if (nameStartsWithWord(lowerName, token)) {
                score = Math.max(score, TechnologySearchRelevance.NAME_STARTS_WITH.score());
            }
            if (containsWord(lowerName, token)) {
                score = Math.max(score, TechnologySearchRelevance.NAME_STARTS_WITH.score());
            }
            if (wordEquals(lowerShortName, token) || containsWord(lowerShortName, token)) {
                score = Math.max(score, TechnologySearchRelevance.SHORT_NAME_MATCH.score());
            }
            if (containsWord(slugAsPhrase, token) || slugSegmentEquals(lowerSlug, token)) {
                score = Math.max(score, TechnologySearchRelevance.SLUG_MATCH.score());
            }
            if (tagsContainExactTerm(tags, token)) {
                score = Math.max(score, TechnologySearchRelevance.TAG_MATCH.score());
            }
            if (containsWord(lowerDescription, token)) {
                score = Math.max(score, TechnologySearchRelevance.DESCRIPTION_MATCH.score());
            }
        }

        return score;
    }

    public static Comparator<LearnTechnology> relevanceComparator(String query) {
        return Comparator
                .comparingInt((LearnTechnology technology) -> score(technology, query)).reversed()
                .thenComparing(LearnTechnology::getName, String.CASE_INSENSITIVE_ORDER);
    }

    public static List<String> tokenize(String query) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        String[] rawTokens = normalizePhrase(query).split(" ");
        List<String> tokens = new ArrayList<>();
        for (String token : rawTokens) {
            if (StringUtils.hasText(token)) {
                tokens.add(token);
            }
        }
        return List.copyOf(tokens);
    }

    public static boolean containsWord(String value, String term) {
        if (!StringUtils.hasText(value) || !StringUtils.hasText(term)) {
            return false;
        }

        String lowerValue = value.toLowerCase(Locale.ROOT);
        String lowerTerm = term.toLowerCase(Locale.ROOT);
        int index = 0;

        while (index <= lowerValue.length()) {
            int found = lowerValue.indexOf(lowerTerm, index);
            if (found < 0) {
                return false;
            }

            boolean validStart = found == 0 || isDelimiter(lowerValue.charAt(found - 1));
            int end = found + lowerTerm.length();
            boolean validEnd = end == lowerValue.length() || isDelimiter(lowerValue.charAt(end));

            if (validStart && validEnd) {
                return true;
            }

            index = found + 1;
        }

        return false;
    }

    public static boolean tagsContainExactTerm(List<String> tags, String term) {
        if (tags == null || tags.isEmpty() || !StringUtils.hasText(term)) {
            return false;
        }

        String lowerTerm = term.toLowerCase(Locale.ROOT);
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .anyMatch(lowerTerm::equals);
    }

    private static boolean matchesAllTokens(
            String name,
            String shortName,
            String description,
            String slug,
            List<String> tags,
            List<String> tokens
    ) {
        for (String token : tokens) {
            if (!tokenMatchesAnyField(name, shortName, description, slug, tags, token)) {
                return false;
            }
        }
        return true;
    }

    private static boolean tokenMatchesAnyField(
            String name,
            String shortName,
            String description,
            String slug,
            List<String> tags,
            String token
    ) {
        String lowerSlug = normalizeField(slug);
        return containsWord(name, token)
                || containsWord(shortName, token)
                || containsWord(description, token)
                || containsWord(lowerSlug.replace('-', ' '), token)
                || slugSegmentEquals(lowerSlug, token)
                || tagsContainExactTerm(tags, token);
    }

    private static boolean slugSegmentEquals(String slug, String token) {
        if (!StringUtils.hasText(slug) || !StringUtils.hasText(token)) {
            return false;
        }

        String lowerSlug = slug.toLowerCase(Locale.ROOT);
        String lowerTerm = token.toLowerCase(Locale.ROOT);

        return lowerSlug.equals(lowerTerm)
                || lowerSlug.startsWith(lowerTerm + "-")
                || lowerSlug.endsWith("-" + lowerTerm)
                || lowerSlug.contains("-" + lowerTerm + "-");
    }

    private static boolean wordEquals(String value, String term) {
        return normalizeField(value).equals(term.toLowerCase(Locale.ROOT));
    }

    private static boolean nameStartsWithWord(String lowerName, String token) {
        String lowerTerm = token.toLowerCase(Locale.ROOT);
        return lowerName.equals(lowerTerm)
                || lowerName.startsWith(lowerTerm + " ")
                || lowerName.startsWith(lowerTerm + "(");
    }

    private static boolean phraseStartsWith(String lowerValue, String normalizedQuery) {
        return lowerValue.equals(normalizedQuery)
                || lowerValue.startsWith(normalizedQuery + " ")
                || lowerValue.startsWith(normalizedQuery + "(");
    }

    private static boolean phraseMatchesAsWords(String lowerValue, String normalizedQuery) {
        if (!StringUtils.hasText(lowerValue) || !StringUtils.hasText(normalizedQuery)) {
            return false;
        }

        int index = 0;
        while (index <= lowerValue.length()) {
            int found = lowerValue.indexOf(normalizedQuery, index);
            if (found < 0) {
                return false;
            }

            boolean validStart = found == 0 || isDelimiter(lowerValue.charAt(found - 1));
            int end = found + normalizedQuery.length();
            boolean validEnd = end == lowerValue.length() || isDelimiter(lowerValue.charAt(end));

            if (validStart && validEnd) {
                return true;
            }

            index = found + 1;
        }

        return false;
    }

    private static String normalizePhrase(String query) {
        return query.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static String normalizeField(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static boolean isDelimiter(char character) {
        return switch (character) {
            case ' ', ',', '(', ')', '-', '.', '/', ':', ';' -> true;
            default -> false;
        };
    }
}
