package com.company.learninghub.communication.service;

import com.company.learninghub.communication.domain.CommunicationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class CommunicationEventSerializer {

    private final ObjectMapper objectMapper;

    public CommunicationEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(CommunicationEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize communication event", ex);
        }
    }

    public CommunicationEvent deserialize(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, CommunicationEvent.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize communication event", ex);
        }
    }
}
