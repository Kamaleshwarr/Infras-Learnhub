package com.company.learninghub.assistant.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

/**
 * Temporary per-request diagnostics for assistant chat tracing.
 */
public final class AssistantChatDiagnostics {

    private static final Logger log = LoggerFactory.getLogger(AssistantChatDiagnostics.class);
    private static final ThreadLocal<Trace> CURRENT = new ThreadLocal<>();

    private AssistantChatDiagnostics() {
    }

    public static void start(String requestId) {
        CURRENT.set(new Trace(requestId));
    }

    public static void end() {
        Trace trace = CURRENT.get();
        if (trace != null) {
            log.info("ASSISTANT_CHAT_DIAG {}", trace.toLogLine());
        }
        CURRENT.remove();
    }

    public static Optional<Trace> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static void recordIntent(String intentType, String normalizedMessage) {
        current().ifPresent(trace -> {
            trace.intentType = intentType;
            trace.normalizedMessage = normalizedMessage;
        });
    }

    public static void recordInjectedClient(String clientClass, String providerName) {
        current().ifPresent(trace -> {
            trace.llmClientClass = clientClass;
            trace.providerName = providerName;
        });
    }

    public static void recordPromptLength(int promptLength) {
        current().ifPresent(trace -> trace.promptLength = promptLength);
    }

    public static void recordLlmHttpStart(String httpUri) {
        current().ifPresent(trace -> {
            trace.httpUri = httpUri;
            trace.requestStart = Instant.now();
        });
    }

    public static void recordLlmHttpEnd(
            int httpStatus,
            long elapsedMs,
            String rawResponse,
            String parsedContent,
            String parsedReasoning,
            String parsedReasoningContent,
            String error,
            Exception exception
    ) {
        current().ifPresent(trace -> {
            trace.requestEnd = Instant.now();
            trace.elapsedMs = elapsedMs;
            trace.httpStatus = httpStatus;
            trace.rawResponse = rawResponse;
            trace.parsedContent = parsedContent;
            trace.parsedReasoning = parsedReasoning;
            trace.parsedReasoningContent = parsedReasoningContent;
            trace.error = error;
            trace.exception = exception == null ? null : exception.getClass().getName() + ": " + exception.getMessage();
        });
    }

    public static void recordFinalResponse(String response) {
        current().ifPresent(trace -> trace.finalResponse = response);
    }

    public static void recordFallbackReturned(String location) {
        current().ifPresent(trace -> trace.fallbackLocations.add(location));
        log.warn("ASSISTANT_CHAT_DIAG fallback returned from {} requestId={}", location, current().map(t -> t.requestId).orElse("unknown"));
    }

    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    public static final class Trace {
        private final String requestId;
        private String intentType;
        private String normalizedMessage;
        private String llmClientClass;
        private String providerName;
        private Integer promptLength;
        private String httpUri;
        private Instant requestStart;
        private Instant requestEnd;
        private Long elapsedMs;
        private Integer httpStatus;
        private String rawResponse;
        private String parsedContent;
        private String parsedReasoning;
        private String parsedReasoningContent;
        private String error;
        private String exception;
        private String finalResponse;
        private final java.util.List<String> fallbackLocations = new java.util.ArrayList<>();

        private Trace(String requestId) {
            this.requestId = requestId;
        }

        private String toLogLine() {
            return "requestId=" + requestId
                    + " intent=" + intentType
                    + " normalizedMessage=" + normalizedMessage
                    + " llmClientClass=" + llmClientClass
                    + " providerName=" + providerName
                    + " promptLength=" + promptLength
                    + " httpUri=" + httpUri
                    + " requestStart=" + requestStart
                    + " requestEnd=" + requestEnd
                    + " elapsedMs=" + elapsedMs
                    + " httpStatus=" + httpStatus
                    + " rawResponse=" + truncate(rawResponse, 1000)
                    + " parsedContent=" + truncate(parsedContent, 500)
                    + " parsedReasoning=" + truncate(parsedReasoning, 500)
                    + " parsedReasoningContent=" + truncate(parsedReasoningContent, 500)
                    + " error=" + error
                    + " exception=" + exception
                    + " fallbackLocations=" + fallbackLocations
                    + " finalResponse=" + truncate(finalResponse, 500);
        }
    }
}
