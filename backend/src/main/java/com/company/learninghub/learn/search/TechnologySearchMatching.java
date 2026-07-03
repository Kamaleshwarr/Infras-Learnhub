package com.company.learninghub.learn.search;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * Whole-term matching rules for technology search.
 * Prevents substring false positives such as matching {@code java} inside {@code JavaScript}.
 */
public final class TechnologySearchMatching {

    private TechnologySearchMatching() {
    }

    public static boolean textContainsWholeTerm(String value, String term) {
        if (!StringUtils.hasText(value) || !StringUtils.hasText(term)) {
            return false;
        }

        String lowerValue = value.toLowerCase(Locale.ROOT);
        String lowerTerm = term.toLowerCase(Locale.ROOT);

        return lowerValue.equals(lowerTerm)
                || lowerValue.startsWith(lowerTerm + " ")
                || lowerValue.endsWith(" " + lowerTerm)
                || lowerValue.contains(" " + lowerTerm + " ")
                || lowerValue.startsWith(lowerTerm + ",")
                || lowerValue.contains(" " + lowerTerm + ",")
                || lowerValue.contains("(" + lowerTerm + ")")
                || lowerValue.contains("(" + lowerTerm + " ");
    }

    public static boolean slugContainsTerm(String slug, String term) {
        if (!StringUtils.hasText(slug) || !StringUtils.hasText(term)) {
            return false;
        }

        String lowerSlug = slug.toLowerCase(Locale.ROOT);
        String lowerTerm = term.toLowerCase(Locale.ROOT);

        return lowerSlug.equals(lowerTerm)
                || lowerSlug.startsWith(lowerTerm + "-")
                || lowerSlug.endsWith("-" + lowerTerm)
                || lowerSlug.contains("-" + lowerTerm + "-");
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

    public static boolean technologyMatches(
            String name,
            String shortName,
            String description,
            String slug,
            List<String> tags,
            String term
    ) {
        return textContainsWholeTerm(name, term)
                || textContainsWholeTerm(shortName, term)
                || textContainsWholeTerm(description, term)
                || slugContainsTerm(slug, term)
                || tagsContainExactTerm(tags, term);
    }
}
