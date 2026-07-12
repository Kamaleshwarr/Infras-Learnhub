package com.company.learninghub.communication.service;

import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.communication.domain.CommunicationEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunicationService {

    private final CommunicationProperties communicationProperties;
    private final CommunicationDispatcher communicationDispatcher;

    public CommunicationService(
            CommunicationProperties communicationProperties,
            CommunicationDispatcher communicationDispatcher
    ) {
        this.communicationProperties = communicationProperties;
        this.communicationDispatcher = communicationDispatcher;
    }

    @Transactional
    public void publish(CommunicationEvent event) {
        if (!communicationProperties.isEnabled()) {
            return;
        }
        communicationDispatcher.dispatch(event);
    }
}
