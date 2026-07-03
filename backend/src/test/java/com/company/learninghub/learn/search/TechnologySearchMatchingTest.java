package com.company.learninghub.learn.search;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologySearchMatchingTest {

    private static final TechnologyRecord JAVA = record(
            "Java", "Java",
            "Enterprise-grade language for backend services, APIs, and distributed systems.",
            "java", List.of("jvm", "oop", "backend")
    );
    private static final TechnologyRecord SPRING_BOOT = record(
            "Spring Boot", "Spring Boot",
            "Production-ready Java applications with convention-over-configuration and a rich ecosystem.",
            "spring-boot", List.of("java", "microservices", "rest-api")
    );
    private static final TechnologyRecord JUNIT = record(
            "JUnit", "JUnit",
            "Unit testing framework for Java applications.",
            "junit", List.of("java", "unit-testing", "tdd")
    );
    private static final TechnologyRecord NODEJS = record(
            "Node.js", "Node.js",
            "JavaScript runtime for building scalable network applications and APIs.",
            "nodejs", List.of("javascript", "api", "runtime")
    );
    private static final TechnologyRecord TYPESCRIPT = record(
            "TypeScript", "TypeScript",
            "Typed superset of JavaScript for safer, more maintainable frontend and full-stack code.",
            "typescript", List.of("javascript", "types", "frontend")
    );
    private static final TechnologyRecord AWS = record(
            "Amazon Web Services (AWS)", "AWS",
            "Cloud computing platform covering compute, storage, networking, and managed services.",
            "aws", List.of("cloud", "infrastructure", "iaas")
    );
    private static final TechnologyRecord DOCKER = record(
            "Docker", "Docker",
            "Platform for building, shipping, and running applications in containers.",
            "docker", List.of("containers", "devops", "images")
    );
    private static final TechnologyRecord REACT = record(
            "React", "React",
            "Component-based library for building interactive user interfaces.",
            "react", List.of("javascript", "ui", "spa")
    );
    private static final TechnologyRecord REACTIVE = record(
            "Reactive Programming", "Reactive",
            "Asynchronous programming model for event-driven and streaming systems.",
            "reactive-programming", List.of("streams", "events")
    );

    @Test
    void javaSearchMatchesExpectedTechnologies() {
        assertMatched("java", JAVA, SPRING_BOOT, JUNIT);
        assertNotMatched("java", NODEJS, TYPESCRIPT);
    }

    @Test
    void javaSearchIsCaseInsensitive() {
        assertThat(matchedSlugs("JAVA")).containsExactlyElementsOf(matchedSlugs("java"));
        assertThat(matchedSlugs("Java")).containsExactlyElementsOf(matchedSlugs("java"));
        assertThat(matchedSlugs("JaVa")).containsExactlyElementsOf(matchedSlugs("java"));
    }

    @Test
    void javaSearchRanksExactNameBeforeDescriptionAndTagMatches() {
        TechnologyRecord descriptionOnly = record(
                "Desc only", "Desc only", "Uses Java in a sentence.", "desc-only", List.of()
        );
        List<TechnologyRecord> ranked = rank("java", JAVA, SPRING_BOOT, JUNIT);

        assertThat(ranked.get(0)).isEqualTo(JAVA);
        assertThat(ranked.subList(1, 3)).containsExactlyInAnyOrder(SPRING_BOOT, JUNIT);
        assertThat(JAVA.score("java")).isGreaterThan(SPRING_BOOT.score("java"));
        assertThat(SPRING_BOOT.score("java")).isGreaterThan(descriptionOnly.score("java"));
    }

    @Test
    void springSearchMatchesSpringBoot() {
        assertMatched("spring", SPRING_BOOT);
        assertNotMatched("spring", JAVA, DOCKER);
    }

    @Test
    void multiWordSpringBootSearchMatchesSpringBoot() {
        assertMatched("spring boot", SPRING_BOOT);
    }

    @Test
    void awsAndAmazonSearchMatchAmazonWebServices() {
        assertMatched("aws", AWS);
        assertMatched("amazon", AWS);
        assertMatched("amazon web services", AWS);
        assertNotMatched("aws", DOCKER, JAVA);
    }

    @Test
    void dockerSearchMatchesDockerOnly() {
        assertMatched("docker", DOCKER);
        assertNotMatched("docker", AWS, JAVA);
    }

    @Test
    void reactSearchMatchesReactButNotReactiveProgramming() {
        assertMatched("react", REACT);
        assertNotMatched("react", REACTIVE);
    }

    @Test
    void textSearchRejectsJavaInsideJavascript() {
        assertThat(TechnologySearchMatching.containsWord(
                "JavaScript runtime for building scalable network applications and APIs.",
                "java"
        )).isFalse();
        assertThat(TechnologySearchMatching.containsWord(
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
        assertThat(TechnologySearchMatching.containsWord("spring boot", "spring")).isTrue();
        assertThat(TechnologySearchMatching.containsWord("spring boot", "java")).isFalse();
    }

    private void assertMatched(String query, TechnologyRecord... expected) {
        List<String> expectedSlugs = Stream.of(expected).map(TechnologyRecord::slug).toList();
        assertThat(matchedSlugs(query, catalog())).containsAll(expectedSlugs);
    }

    private void assertNotMatched(String query, TechnologyRecord... excluded) {
        List<String> excludedSlugs = Stream.of(excluded).map(TechnologyRecord::slug).toList();
        assertThat(matchedSlugs(query, catalog())).doesNotContainAnyElementsOf(excludedSlugs);
    }

    private List<String> matchedSlugs(String query) {
        return matchedSlugs(query, catalog());
    }

    private List<String> matchedSlugs(String query, List<TechnologyRecord> technologies) {
        return technologies.stream()
                .filter(technology -> technology.matches(query))
                .sorted(Comparator.comparingInt((TechnologyRecord technology) -> technology.score(query)).reversed()
                        .thenComparing(TechnologyRecord::name, String.CASE_INSENSITIVE_ORDER))
                .map(TechnologyRecord::slug)
                .toList();
    }

    private List<TechnologyRecord> rank(String query, TechnologyRecord... technologies) {
        return Stream.of(technologies)
                .filter(technology -> technology.matches(query))
                .sorted(Comparator.comparingInt((TechnologyRecord technology) -> technology.score(query)).reversed()
                        .thenComparing(TechnologyRecord::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<TechnologyRecord> catalog() {
        return List.of(JAVA, SPRING_BOOT, JUNIT, NODEJS, TYPESCRIPT, AWS, DOCKER, REACT, REACTIVE);
    }

    private static TechnologyRecord record(
            String name,
            String shortName,
            String description,
            String slug,
            List<String> tags
    ) {
        return new TechnologyRecord(name, shortName, description, slug, tags);
    }

    private record TechnologyRecord(
            String name,
            String shortName,
            String description,
            String slug,
            List<String> tags
    ) {
        private boolean matches(String query) {
            return TechnologySearchMatching.matches(name, shortName, description, slug, tags, query);
        }

        private int score(String query) {
            return TechnologySearchMatching.score(name, shortName, description, slug, tags, query);
        }
    }
}
