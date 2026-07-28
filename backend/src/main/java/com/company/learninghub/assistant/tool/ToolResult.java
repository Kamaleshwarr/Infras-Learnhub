package com.company.learninghub.assistant.tool;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ToolResult {

    private final String text;
    private final Object structuredData;
    private final List<ToolCitation> citations;
    private final List<ToolSourceReference> sourceReferences;
    private final String markdown;
    private final Map<String, Object> metadata;

    private ToolResult(
            String text,
            Object structuredData,
            List<ToolCitation> citations,
            List<ToolSourceReference> sourceReferences,
            String markdown,
            Map<String, Object> metadata
    ) {
        this.text = text;
        this.structuredData = structuredData;
        this.citations = citations == null ? List.of() : List.copyOf(citations);
        this.sourceReferences = sourceReferences == null ? List.of() : List.copyOf(sourceReferences);
        this.markdown = markdown;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static ToolResult text(String text) {
        return new ToolResult(text, null, List.of(), List.of(), null, Map.of());
    }

    public static ToolResult structured(String text, Object structuredData) {
        return new ToolResult(text, structuredData, List.of(), List.of(), null, Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public String text() {
        return text;
    }

    public Object structuredData() {
        return structuredData;
    }

    public List<ToolCitation> citations() {
        return citations;
    }

    public List<ToolSourceReference> sourceReferences() {
        return sourceReferences;
    }

    public String markdown() {
        return markdown;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    public static final class Builder {
        private String text;
        private Object structuredData;
        private List<ToolCitation> citations = List.of();
        private List<ToolSourceReference> sourceReferences = List.of();
        private String markdown;
        private Map<String, Object> metadata = Map.of();

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder structuredData(Object structuredData) {
            this.structuredData = structuredData;
            return this;
        }

        public Builder citations(List<ToolCitation> citations) {
            this.citations = citations == null ? List.of() : List.copyOf(citations);
            return this;
        }

        public Builder sourceReferences(List<ToolSourceReference> sourceReferences) {
            this.sourceReferences = sourceReferences == null ? List.of() : List.copyOf(sourceReferences);
            return this;
        }

        public Builder markdown(String markdown) {
            this.markdown = markdown;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
            return this;
        }

        public ToolResult build() {
            return new ToolResult(text, structuredData, citations, sourceReferences, markdown, metadata);
        }
    }

    public record ToolCitation(
            String label,
            String reference
    ) {
        public ToolCitation {
            Objects.requireNonNull(label, "label is required");
            Objects.requireNonNull(reference, "reference is required");
        }
    }

    public record ToolSourceReference(
            String sourceType,
            String sourceId,
            String label
    ) {
        public ToolSourceReference {
            Objects.requireNonNull(sourceType, "sourceType is required");
            Objects.requireNonNull(sourceId, "sourceId is required");
        }
    }
}
