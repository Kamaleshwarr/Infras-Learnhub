package com.company.learninghub.assistant.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class MockLlmClient implements LlmClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockLlmClient.class);
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s]+");

    private static final String UNKNOWN_FALLBACK =
            "I don't currently have enough information to answer this. "
                    + "Future versions will support broader AI knowledge.";

    private static final String SYSTEM_PROMPT =
            "You are the Engineering Learning Hub assistant. Provide concise, helpful explanations. "
                    + "Never invent user-specific platform data such as certifications, projects, or rankings.";

    @Override
    public LlmCompletionResult complete(LlmCompletionRequest request) {
        String userMessage = extractLastUserMessage(request);
        LOGGER.debug(
                "Assistant LLM (mock mode). systemPromptLength={}, messageCount={}, userMessageLength={}",
                request.systemPrompt() == null ? 0 : request.systemPrompt().length(),
                request.messages() == null ? 0 : request.messages().size(),
                userMessage.length()
        );
        return LlmCompletionResult.success(generateResponse(userMessage), "mock-mode");
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

    @Override
    public String providerName() {
        return "mock";
    }

    String generateResponse(String userMessage) {
        String normalized = normalize(userMessage);
        if (!StringUtils.hasText(normalized)) {
            return UNKNOWN_FALLBACK;
        }

        if (matchesAny(normalized, "submit certificate", "submit a certificate", "certificate submission")) {
            return "To submit a certificate, open Submit Certificate from the dashboard or go to "
                    + "/submissions/new. Choose an active learning initiative, upload your certificate file, "
                    + "and submit it for review. You can track status from My Submissions.";
        }

        if (matchesAny(normalized, "spring boot")) {
            return "Spring Boot is an open-source Java framework for building production-ready applications "
                    + "with minimal configuration. It provides auto-configuration, embedded servers, and "
                    + "starters for common concerns such as web APIs, data access, and security. "
                    + "In Engineering Learning Hub, you can explore Spring Boot learning paths from the Learn module.";
        }

        if (matchesAny(normalized, "docker")) {
            return "Docker is a platform for packaging applications and their dependencies into portable "
                    + "containers. Containers run consistently across environments and are widely used for "
                    + "local development, CI/CD, and deployment workflows.";
        }

        if (matchesAny(normalized, "react")) {
            return "React is a JavaScript library for building user interfaces using reusable components. "
                    + "It is commonly used for single-page applications and pairs well with TypeScript and "
                    + "component libraries such as Material UI.";
        }

        if (matchesAny(normalized, "java")) {
            return "Java is a widely used object-oriented programming language known for portability, "
                    + "strong typing, and a mature ecosystem. Engineering Learning Hub includes Java learning "
                    + "roadmaps in the Learn catalog.";
        }

        if (isKnowledgeQuery(normalized)) {
            return "I can explain general technology concepts and guide you through Engineering Learning Hub "
                    + "workflows. For account-specific details such as certifications or leaderboard rank, "
                    + "ask directly (for example: \"my certifications\" or \"my leaderboard rank\").";
        }

        return UNKNOWN_FALLBACK;
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

    private String extractLastUserMessage(LlmCompletionRequest request) {
        if (request.messages() == null || request.messages().isEmpty()) {
            return "";
        }
        for (int index = request.messages().size() - 1; index >= 0; index--) {
            LlmCompletionRequest.LlmMessage message = request.messages().get(index);
            if ("user".equalsIgnoreCase(message.role()) && StringUtils.hasText(message.content())) {
                return message.content();
            }
        }
        return "";
    }

    private String normalize(String message) {
        if (!StringUtils.hasText(message)) {
            return "";
        }
        String normalized = message.trim().toLowerCase(Locale.ROOT);
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll(" ");
        return normalized.replaceAll("\\s+", " ").trim();
    }

    public static LlmCompletionRequest knowledgeRequest(String userMessage) {
        return new LlmCompletionRequest(
                SYSTEM_PROMPT,
                java.util.List.of(new LlmCompletionRequest.LlmMessage("user", userMessage))
        );
    }
}
