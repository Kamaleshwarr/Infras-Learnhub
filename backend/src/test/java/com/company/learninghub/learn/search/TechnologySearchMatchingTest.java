package com.company.learninghub.learn.search;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologySearchMatchingTest {

    @Test
    void javaSearchMatchesExpectedCatalogTechnologies() {
        assertThat(technologyMatches(
                "Java",
                "Java",
                "Enterprise-grade language for backend services, APIs, and distributed systems.",
                "java",
                List.of("jvm", "oop", "backend"),
                "java"
        )).isTrue();

        assertThat(technologyMatches(
                "Spring Boot",
                "Spring Boot",
                "Production-ready Java applications with convention-over-configuration and a rich ecosystem.",
                "spring-boot",
                List.of("java", "microservices", "rest-api"),
                "java"
        )).isTrue();

        assertThat(technologyMatches(
                "JUnit",
                "JUnit",
                "Unit testing framework for Java applications.",
                "junit",
                List.of("java", "unit-testing", "tdd"),
                "java"
        )).isTrue();
    }

    @Test
    void javaSearchDoesNotMatchJavascriptTechnologies() {
        assertThat(technologyMatches(
                "Node.js",
                "Node.js",
                "JavaScript runtime for building scalable network applications and APIs.",
                "nodejs",
                List.of("javascript", "api", "runtime"),
                "java"
        )).isFalse();

        assertThat(technologyMatches(
                "TypeScript",
                "TypeScript",
                "Typed superset of JavaScript for safer, more maintainable frontend and full-stack code.",
                "typescript",
                List.of("javascript", "types", "frontend"),
                "java"
        )).isFalse();
    }

    @Test
    void textContainsWholeTermRejectsJavaInsideJavascript() {
        assertThat(TechnologySearchMatching.textContainsWholeTerm(
                "JavaScript runtime for building scalable network applications and APIs.",
                "java"
        )).isFalse();

        assertThat(TechnologySearchMatching.textContainsWholeTerm(
                "Production-ready Java applications with convention-over-configuration and a rich ecosystem.",
                "java"
        )).isTrue();
    }

    @Test
    void tagsRequireExactTermMatch() {
        assertThat(TechnologySearchMatching.tagsContainExactTerm(List.of("javascript", "api"), "java"))
                .isFalse();
        assertThat(TechnologySearchMatching.tagsContainExactTerm(List.of("java", "microservices"), "java"))
                .isTrue();
    }

    @Test
    void slugMatchesWholeSegmentsOnly() {
        assertThat(TechnologySearchMatching.slugContainsTerm("java", "java")).isTrue();
        assertThat(TechnologySearchMatching.slugContainsTerm("spring-boot", "java")).isFalse();
        assertThat(TechnologySearchMatching.slugContainsTerm("not-java", "java")).isTrue();
    }

    private boolean technologyMatches(
            String name,
            String shortName,
            String description,
            String slug,
            List<String> tags,
            String term
    ) {
        return TechnologySearchMatching.technologyMatches(name, shortName, description, slug, tags, term);
    }
}
