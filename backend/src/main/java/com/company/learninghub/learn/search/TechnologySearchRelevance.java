package com.company.learninghub.learn.search;

/**
 * Relevance tiers for catalog technology search results.
 * Higher values rank earlier when multiple technologies match a query.
 */
public enum TechnologySearchRelevance {

    EXACT_NAME_MATCH(1_000),
    NAME_STARTS_WITH(800),
    SHORT_NAME_MATCH(600),
    SLUG_MATCH(400),
    TAG_MATCH(200),
    DESCRIPTION_MATCH(100);

    private final int score;

    TechnologySearchRelevance(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }
}
